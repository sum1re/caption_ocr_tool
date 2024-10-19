import React from 'react'
import { useForm } from 'react-hook-form'

type CompareAlgorithm = 'SSIM' | 'PSNR'
type MatRetentionPolicy = 'FIRST_IN' | 'LAST_IN' | 'WHITEST' | 'BLACKEST' | 'BALANCED'

type FontColor = {
  primary: string;    // PrimaryColour
  secondary: string;  // SecondaryColour
  outline: string;    // OutlineColour or TertiaryColor for SSA
  shadow: string;     // renamed to BackColour
}

type FontMargin = {
  left: number;     // MarginL
  right: number;    // MarginR
  vertical: number; // MarginV
}

type FontAlignment = 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9

type FontStyle = {
  friendlyName: string;
  fontFamily: string;
  fontSize: string;
  bold: boolean;
  italic: boolean;
  underline: boolean;
  strikeout: boolean;
  color: FontColor;
  margins: FontMargin;
  alignment: FontAlignment;
}

type AppSettings = {
  filterInterval: number;
  compareAlgorithm: CompareAlgorithm;
  similarThreshold: Record<CompareAlgorithm, number>;
  pixelThreshold: number;
  matRetentionPolicy: MatRetentionPolicy;
  fontStyleStorage: FontStyle[];
}

export function Settings(): React.ReactElement {
  const {} = useForm<AppSettings>()
  return (
    <>
      TODO('finish-setting-form')
    </>
  )
}