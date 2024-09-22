export type AdaptiveMethod = 'ADAPTIVE_THRESH_MEAN_C' | 'ADAPTIVE_THRESH_GAUSSIAN_C';

export type ThresholdType = 'THRESH_BINARY' | 'THRESH_BINARY_INV';

export type Operation =
  'ADD'
  | 'SUBTRACT'
  | 'MULTIPLY'
  | 'DIVIDE'
  | 'ABS_DIFF'
  | 'BITWISE_AND'
  | 'BITWISE_OR'
  | 'BITWISE_NOT'
  | 'BITWISE_XOR'
  | 'MAX'
  | 'MIN';

export type Border =
  'BORDER_CONSTANT'
  | 'BORDER_REPLICATE'
  | 'BORDER_REFLECT'
  | 'BORDER_WRAP'
  | 'BORDER_REFLECT_101'
  | 'BORDER_TRANSPARENT'
  | 'BORDER_DEFAULT'
  | 'BORDER_ISOLATED';

export type ColorType =
  'COLOR_BGR2GRAY'
  | 'COLOR_BGR2HLS'
  | 'COLOR_BGR2HLS_FULL'
  | 'COLOR_BGR2HSV'
  | 'COLOR_BGR2HSV_FULL'
  | 'COLOR_GRAY2BGR'
  | 'COLOR_HLS2BGR'
  | 'COLOR_HLS2BGR_FULL'
  | 'COLOR_HSV2BGR'
  | 'COLOR_HSV2BGR_FULL';

export type Depth =
  'CV_8S'
  | 'CV_8U'
  | 'CV_16F'
  | 'CV_16S'
  | 'CV_16U'
  | 'CV_32F'
  | 'CV_32S'
  | 'CV_64F';

export  type MorphType =
  'MORPH_ERODE'
  | 'MORPH_DILATE'
  | 'MORPH_OPEN'
  | 'MORPH_CLOSE'
  | 'MORPH_GRADIENT'
  | 'MORPH_TOPHAT'
  | 'MORPH_BLACKHAT'
  | 'MORPH_HITMISS';

export type  MorphShape = 'MORPH_RECT' | 'MORPH_CROSS' | 'MORPH_ELLIPSE';

export type NumberPair = {
  first: number;
  second: number;
}

export type CropRange = {
  upperLeft: NumberPair;
  lowerRight: NumberPair;
}

export type Morphology = {
  morphType: MorphType;
  morphShape: MorphShape;
  kernel: NumberPair;
  shapeAnchor: NumberPair;
  morphAnchor: NumberPair;
  iteration: number;
  border: Boolean;
}

export type AdaptiveBinarization = {
  maxValue: number;
  adaptiveMethod: AdaptiveMethod;
  thresholdType: ThresholdType;
  blockSize: number;
  constant: number;
}

export type FixedBinarization = {
  maxValue: number;
  thresholdValue: number;
  thresholdType: ThresholdType;
}

export type BilateralFilter = {
  diameter: number;
  sigmaColor: number;
  sigmaSpace: number;
  border: Border;
}

export type BoxFilter = {
  kernel: NumberPair;
  anchor: NumberPair;
  normalize: boolean;
  border: Border;
}

export type GaussianBlur = {
  kernel: NumberPair;
  sigma: NumberPair;
  border: Border;
}

export type MedianBlur = {
  kernelSize: number;
}

export type HLSRange = {
  mask: boolean;
  hue: NumberPair;
  lightness: NumberPair;
  saturation: NumberPair;
}

export type HSVRange = {
  hue: NumberPair;
  saturation: NumberPair;
  value: NumberPair;
}

export type ConvertColor = {
  colorType: ColorType;
}

export type ConvertDepth = {
  depth: Depth
}

export type EqualizeHist = {
  index: number;
}
