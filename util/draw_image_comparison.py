import matplotlib.pyplot as plt
import argparse

parser = argparse.ArgumentParser(description='Plot a point cloud')
parser.add_argument('ref_file', type=str, help='name of the input file containing the reference points')
parser.add_argument('cand_file', type=str, help='name of the input file containing the candidate points')

args = vars(parser.parse_args())

def read_points(file_name, r, g, b):
    lines = []
    with open(file_name, 'r') as in_file:
        lines = in_file.readlines()

    xs = []
    ys = []

    raw_alphas = []
    cs = []

    num_lines_metadata = int(lines[0].split(' ')[1].strip())
    points = lines[num_lines_metadata+2::]
    for point in points:
        # Use grayscale value as alpha
        x, y, a = list(map(float, point.split(', ')))
        xs.append(x)
        ys.append(y)
        raw_alphas.append(a)

    # https://stackoverflow.com/a/5732390
    # this SHOULD be mapping the range of input grayscale values (which sum to 1
    # and are thus tiny) to the range [0.1, 1.0] so that you can actually see
    # things in the output image.
    min_alpha = min(raw_alphas)
    slope = (1.0 - 0.1) / (max(raw_alphas) - min_alpha)
    for a in raw_alphas:
        adjusted_alpha = (0.1 + slope * (a - min_alpha))
        cs.append((r, g, b, adjusted_alpha))

    return (xs, ys, cs)

ref_x, ref_y, ref_c = read_points(args['ref_file'], 0.5, 0.5, 0.5)
cand_x, cand_y, cand_c = read_points(args['cand_file'], 1.0, 0, 0)

plt.scatter(ref_x, ref_y, c=ref_c)
plt.scatter(cand_x, cand_y, c=cand_c)

plt.xlim(0, 30)
plt.ylim(0, 30)

ax = plt.gca()
ax.invert_yaxis()

ref_name = args['ref_file'].split('/')[-1][0:-4]
cand_name = args['cand_file'].split('/')[-1][0:-4]

plt.savefig(f"{ref_name}-{cand_name}.png")
