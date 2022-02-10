# ITEC 498: Optimal Transport and Image Recognition
Optimal Transport has already been studied in the context of image recognition.
However, a less studied question is how to make optimal transport more resilient
to transformations such as shifting, scaling, and rotation. Two otherwise identical
images could be classified as very different if one of them was simply shifted over
slightly. The object of this independent study is to identify, test, and report on
a strategy for making optimal transport *translation invariant* in the context of
image recognition.
## Potential Strategies
### Particle Swarm Optimization
PSO is a general solution technique for optimization problems. PSO is defined by a 
feasible region F in R^n (an n-dimensional vector space) and a cost function
`c: F -> R`.

Candidate solutions to the problem are modeled as set of `n` dimensional vectors.
Each solution is called a "particle" and the entire set is the "swarm". At a given
iteration t, each particle has some position (set of parameters to the cost function `c`)
and velocity.

The particles are initially placed in the space with any location and velocity we like.
Each iteration, every particle receives a new velocity that is computed based on a number
of things, such as the best solution seen thus far, the "inertia" of the particle, as well
as some random noise. Over time, the particles converge on an optimal
solution to the problem.

In the context of comparing two images, the feasible region describes a set of potential
transformations we could apply to one of the images (the other remains fixed), while the 
cost function is the "distance" between the two images computed via optimal transport.
Using optimal transport as a metric for how close the images are, we can optimize for a 
set of transformations that most closely register the two images.