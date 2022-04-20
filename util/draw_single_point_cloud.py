import matplotlib.pyplot as plt
import argparse

parser = argparse.ArgumentParser(description='Plot a point cloud')
parser.add_argument('input_file', type=str, help='name of the input file containing the points')

args = vars(parser.parse_args())

def read_points(file_name, color):
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

    return (xs, ys, cs, grayscaleValues)

x, y, c, gs = read_points(args['input_file'], "#555555")

plt.scatter(x, y, c=c, alpha=gs)

plt.xlim(0, 30)
plt.ylim(0, 30)

ax = plt.gca()
ax.invert_yaxis()

name = args['input_file'].split('/')[-1][0:-4]

plt.savefig(f"{name}.png")
