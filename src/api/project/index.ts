import type { uuid } from '../../utils/uuid'

export type Project = {
  id: uuid;
  name: string;
}

export type CaptionRow = {
  id: string;
  start: string;
  end: string;
  img: string;
  caption: string;
}

export type PreviewResult = {
  isMetRequire: boolean;
  channel?: number;
  causeBy?: string;
  mat: string;
}