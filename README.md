# Licensing
Currently, this project is deliberately *unlicensed*, meaning it is under exclusive copyright. If you are interested in using or modifying this source code, feel free to open an issue in this repository and I'm happy to consider licensing it.

Unless otherwise noted:
Copyright 2022, Jordan Seiler, All rights reserved.

The contents of the `src/org/ru/ot` folder are the only exception to the above copyright notice, and are under separate copyright by the original author(s). Specific details and copyright are displayed prominently in relevant files.

# MNIST Digit Classifier: Now with 100% less machine learning!
This repository contains the results of my undergraduate thesis project, conducted during the Spring semester of 2022. This README contains a non-exhaustive overview of the technique and its results.

## Premise
[Optimal Transport](https://en.wikipedia.org/wiki/Transportation_theory_(mathematics)#Mines_and_factories) (OT) has already been studied in the context of image recognition. That is, given two images, you can use OT to compute a "similarity score" between them, thus providing a mechanism for classifying images based on their similarity to known references.

However, a less studied question is how to make OT more resilient to transformations such as shifting, scaling, and rotation. Two otherwise identical images could be classified as very different if one of them is simply shifted slightly. The objective of my thesis project was to identify, test, and report on a strategy for making OT "transformation invariant" in the context of image recognition.

In order to keep the scope of the thesis manageable, the task of "image recognition" was reduced in scope to only consider handwritten digits from the [MNIST dataset](https://en.wikipedia.org/wiki/MNIST_database), as opposed to any arbitrary image.

## Fundamental Idea
In a perfect world, the process of classifying a digit using OT is relatively simple. You take the digit you want to classify (called the candidate) and you compare it to a reference images for all the digits 0 through 9. You examine the similarity scores for all ten comparisons, and then label the candidate based on the candidate-to-reference comparison with the best score. If your candidate is most similar to a 7 (based on OT), then you label it as a 7, etc.

But we don't live in a perfect world. People write digits in all kinds of different ways. Sometimes they're slanted, or larger/smaller than the reference, or perhaps the digit is squashed vertically or horizontally. Specifically, I'm referring to transformations of a digit that boil down to some combination of shifting, rotation, or x/y scaling. I'll refer to these differences as "incidental differences", as opposed to "inherent differences". A 2 and an 8 are *inherently different* because they are different digits with different shapes. But a 2 and a slightly shifted 2 are just "incidentally different" due to the shifting.

What we want is some strategy that corrects for the incidental differences between candidate and reference images. A process I will refer to as "registration", after the [concept in color printing](https://en.wikipedia.org/wiki/Printing_registration). If we had some "magic oracle" that could line up the candidate and reference as closely as possible before classification, then OT would only be reporting on the inherent differences between the two digits.

## Registration Strategy
### Naive Approach
What's the simplest possible strategy that could solve this problem? If we place upper and lower bounds on the amount that the candidate will need to be transformed (e.g. the candidate may only be rotated at most 180 degrees, shifted at most 1 inch in any direction, etc.) then we could simply check *every possible combination* of shift, rotation, and scaling, and then select the best score for each reference.

Clearly that's not computationally feasible. But what if we had some way to quickly and intelligently *search* the space of all possible transformations that yields the best similarity score? Put in other words: "Search the space of all linear transformations that, when applied to this candidate image, gives the best OT similarity score when compared to this reference".

Fortunately, we *do* have a way to search the transformation space, and it's called Particle Swarm Optimization.

### Particle Swarm Optimization
[Particle Swarm Optimization](https://en.wikipedia.org/wiki/Particle_swarm_optimization) (PSO) is a heuristic technique for solving a broad class of optimization problems. In plain English, suppose you have some problem that takes `N` numeric inputs and produces a single numeric output. You want to find the set of inputs to your problem that produces the *lowest* output (you could also look for the *highest* output, as that turns out to be the same problem). How could you accomplish this? For even a small number of inputs, the number of possible combinations will be huge, and the inputs to your problem may interact in complicated ways that are not easy to predict.

PSO allows you to find candidate solutions to such problems, even when the number of inputs is very large (dozens or even hundreds). Because PSO is a heuristic, it does not guarantee that it will find an optimal solution, but in general, once PSO has been tuned to your specific problem, it performs quite well.

That's all well and good, but how does PSO work? Here's a very general explanation:

An instance of PSO is defined two things. First, a "feasible region" `F`, which is a subset of an `N`-dimensional vector space. The dimensionality of the space you're searching is exactly the same as the number of inputs to the function (problem) you want to optimize. The region `F` simply describes all of the possible inputs that could be valid, and it may be the entirety of the vector space (no potential solutions are invalid). Second, the function you want to optimize, which takes a vector from `F` as input, and produces a single real number as output. We call this function the "cost function".

Candidate solutions to the problem are modeled as a set of `N` dimensional vectors that live in `F`. Each of these candidates is called a "particle" and the entire set is the "swarm". At any point in time, each particle has some position (which we will use as input to the cost function) and velocity. Each particle also has a "neighborhood", which is a set of other particles that all talk to each other over the course of PSO. This project uses the simplest neighborhood model, which is a global neighborhood. Every particle can talk to every other particle.

There is no one way to initialize the swarm in `F`. The simplest approach (and the one this project uses) is to place the particles randomly in `F` and give them random initial velocities.

Every iteration, each particle computes the cost function at its current location, updates its velocity based on some simple rules, and then moves to a new location based on its velocity. The components of the particles new velocity are:
1. The particle's inertia :: A tendency to continue exploring in the direction the particle is already moving
2. A pull towards the particle's personal best score (this pull is augmented with some random noise to induce more exploration)
3. A pull towards the particle's neighborhood best score (this pull is also augmented with random noise)

The combination of all three of these values causes the swarm to broadly explore the search space, while also converging towards optimal solutions. You can decide to stop PSO in a variety of ways, here are just a few:
1. Stop the process after a set number of iterations
2. Stop the process when you're close enough to a fixed value (if you know the value you want to hit)
3. Stop the process when the global best has stopped converging

In the context of comparing two images, the feasible region describes a set of potential transformations we could apply to one of the images (the other remains fixed), while the cost function is the "distance" between the two images computed via optimal transport. Using optimal transport as a metric for how close the images are, we can optimize for a set of transformations that most closely register the two images.

## The Big Picture
How are all the pieces put together to create a digit classifier? Here's a _very general_ overview:
First, you must create reference images for each of the digits. This was done by selecting many images of the same type out of the "training" MNIST dataset, and then averaging them. These "average digits" are referred to as "heatmaps".

Then, to classify one digit:
1. Compare the digit to each of your reference digits
2. Label the candidate with the type of the reference that achieved the best fitness score

To compare two images/digits:
1. Preprocess the image
  1. Convert the input image to a point-cloud
  2. Normalize the grayscale values of the point-cloud so they sum to 1 (necessary for running OT)
  3. Balance the input image and reference image with "dud points" so they have the same number of points (necessary for running OT)
2. Execute PSO with OT as the cost function
  1. 5-dimensional search space (shift in X, shift in Y, rotation in radians, scaling factor in X, scaling factor in Y)
  2. Search for the set of transformations that results in the best OT score
3. Report the best score found by PSO

There are plenty more details left out of the above explanation, but I'll have to save those for another document.

## Technique Results
To test, 100 candidate digits were selected for each digit type (1,000 images total). These images had no overlap with the digits that were used to create the references. The test images were classified twice:
1. With no random transformation applied :: 76% average accuracy
2. With random initial transformations applied :: 73% average accuracy

Each classification took about 2-20 seconds (the variability is a result of how long it takes PSO to stop converging), and performance depends on the number of available CPU cores, as the 10 candidate-reference comparisons are done concurrently.

Again, there are plenty more details to be discussed, but I'll have to leave those for another document.

# Project/Implementation Notes
Usage of this project is subject to applicable copyright law and licensing. This section is included to explain why the project is not "complete" in the sense that it is not immediately runnable as-is.

This actual implementation of the technique is, unfortunately, not set up such that it's easy for just anyone to run. In particular, the input data is expected to live in an `img` folder that is omitted from this repository in order to save on space. Supposing you wanted to run this project, you would need to prepare the MNIST data set using a tool I wrote called [Scribe](https://github.com/JDSeiler/scribe). Scribe reads in the binary formatted MNIST images and outputs them as BMP images, organized by their type.

You will also need the heatmap reference images. I'm writing this README about a year after I originally did this project, and I cannot recall how I actually prepared the heatmaps. I suspect I made some one-off modification to Scribe that I never put in version control. In lieu of a script that's capable of producing the heatmaps, I've added them to this repository. You can find them in the `references` folder.

The `Main.java` file is pretty explicit about what files it's loading, so it shouldn't be too hard to get all the images in the right spot.
You'll also find, if you read the code, that there is a substantial amount of "dead code". This is because over the course of the project I would change the contents of `Main.java` (or change different hardcoded variables in certain files) to perform different tests or debugging tasks. Ideally, these sorts of things would get extracted into a CLI interface, but that was too much work at the time for no benefit to my work, so I never did it.
