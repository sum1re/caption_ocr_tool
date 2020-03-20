## Module

This document only has some information that can not find in [OpenCV JavaDoc](https://docs.opencv.org/4.2.0/javadoc), please refer to JavaDoc for mor information.

- [crop](#crop)
- [arithmetic_operations](#arithmetic_operations)
- [max_ccl_filter](#max_ccl_filter)
- [min_ccl_filter](#min_ccl_filter)

### crop

|Module Parameters | Description|
|- | -|
|U-L X/Y | The (X,Y) direction in upper-left of the caption's region|
|L-R X/Y | The (X,Y) direction in lower-right of the caption's region|
|Show Region | Draw a board for the region.|

### arithmetic_operations

|Module Parameters | Description|
|- | -|
|Object 1/2 Index | The object's index, this module will not work until *cache* is enabled.|

### max_ccl_filter

|Module Parameters | Description|
|- | -|
|Area | Remove the connected-component labeling if its area larger than this value. The connected-component labeling must be white and the image is binary.|

### min_ccl_filter

|Module Parameters | Description|
|- | -|
|Area | Remove the connected-component labeling if its area less than this value. The connected-component labeling must be white and the image is binary.|
