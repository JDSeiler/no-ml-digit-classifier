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
    cs = []

    num_lines_metadata = int(lines[0].split(' ')[1].strip())
    points = lines[num_lines_metadata+2::]
    for point in points:
        x, y = list(map(float, point.split(', ')))
        xs.append(x)
        ys.append(y)
        cs.append(color)

    return (xs, ys, cs)

x, y, c = read_points(args['input_file'], "#555555")

plt.scatter(x, y, c=c)

plt.xlim(0, 30)
plt.ylim(0, 30)

ax = plt.gca()
ax.invert_yaxis()

name = args['input_file'].split('/')[-1][0:-4]

plt.savefig(f"{name}.png")
