import matplotlib.pyplot as plt
import argparse

parser = argparse.ArgumentParser(description='Plot a point cloud')
parser.add_argument('ref_file', type=str, help='name of the input file containing the reference points')
parser.add_argument('cand_file', type=str, help='name of the input file containing the candidate points')

args = vars(parser.parse_args())

def read_points(file_name, color):
    lines = []
    with open(file_name, 'r') as in_file:
        lines = in_file.readlines()

    xs = []
    ys = []
    cs = []

    num_lines_metadata = int(lines[0].split(' ')[1].strip())
    points = lines[num_lines_metadata+2::]
    for point in points:
        x, y = list(map(float, point.split(', ')))
        xs.append(x)
        ys.append(y)
        cs.append(color)

    return (xs, ys, cs)

ref_x, ref_y, ref_c = read_points(args['ref_file'], "#555555")
cand_x, cand_y, cand_c = read_points(args['cand_file'], '#db2525')

plt.scatter(ref_x, ref_y, c=ref_c)
plt.scatter(cand_x, cand_y, c=cand_c)

plt.xlim(0, 30)
plt.ylim(0, 30)

ax = plt.gca()
ax.invert_yaxis()

ref_name = args['ref_file'].split('/')[-1][0:-4]
cand_name = args['cand_file'].split('/')[-1][0:-4]

plt.savefig(f"{ref_name}-{cand_name}.png")
