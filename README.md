# ImageClassifier
A simple image classifier using the CIFAR-10 labeled image dataset. CIFAR-10 images are 32 by 32 pixels and are labelled with one of ten classes. Currently includes a simple k-nearest-neighbor classifier that achieves about 34% accuracy on the test set. Future plans include implementing more accurate SVM, softmax, or neural network classification techniques.

To configure and run the classifier using the Swing GUI, run `src/main/gui/RunGUIClassifier.main()`. The classifier can also be run non-interactively using  `RunClassifier.main()`.

## Credits
CIFAR-10 dataset collected by Alex Krizhevsky, Vinod Nair, and Geoffrey Hinton.
http://www.cs.toronto.edu/~kriz/cifar.html

Technical report on dataset: [Learning Multiple Layers of Features from Tiny Images](http://www.cs.toronto.edu/~kriz/learning-features-2009-TR.pdf), Alex Krizhevsky, 2009.

## Licenses
### Apache Commons Collections

Copyright 2001-2017 The Apache Software Foundation

This product includes software developed at the Apache Software Foundation (http://www.apache.org/).
