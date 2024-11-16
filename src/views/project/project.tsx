import { Box, Button, ButtonGroup, Grid2 as Grid } from '@mui/material'
import { green, purple } from '@mui/material/colors'
import { useWavesurfer } from '@wavesurfer/react'
import { useAtom, useSetAtom } from 'jotai'
import { atomWithStorage } from 'jotai/vanilla/utils'
import React, { useCallback, useEffect, useMemo, useRef } from 'react'
import { useParams } from 'react-router-dom'
import type { WaveSurferOptions } from 'wavesurfer.js'
import HoverPlugin from 'wavesurfer.js/plugins/hover'
import RegionsPlugin from 'wavesurfer.js/plugins/regions'
import TimelinePlugin from 'wavesurfer.js/plugins/timeline'
import ZoomPlugin from 'wavesurfer.js/plugins/zoom'
import { projectIdAtom } from '../../provider/atom-provider'
import { formatTime, formatTimeline } from '../../utils/common'
import { type uuid } from '../../utils/uuid'

type MediaOptions = Omit<WaveSurferOptions, 'container' | 'plugins' | 'media' | 'mediaControls'>;

const mediaOptionsAtom = atomWithStorage<MediaOptions>('media-options', {
  audioRate: 1.0,
  autoCenter: true,
  autoScroll: true,
  autoplay: false,
  normalize: false,
  cursorColor: purple['500'],
  cursorWidth: 2,
  waveColor: green['500'],
  progressColor: green['800'],
  barRadius: 3,
  barHeight: 2.5,
  minPxPerSec: 50,
})

export function Project(): React.ReactElement {
  const wavesurferRef = useRef(null)
  const videoRef = useRef<HTMLVideoElement | null>(null)
  const { projectId } = useParams()
  const setProjectId = useSetAtom(projectIdAtom)
  const [mediaOptions, setMediaOptions] = useAtom(mediaOptionsAtom)
  const regionsPlugin = useMemo(() => RegionsPlugin.create(), [])
  const { wavesurfer, isPlaying, currentTime } = useWavesurfer({
    ...mediaOptions,
    container: wavesurferRef,
    height: 100,
    plugins: useMemo(() => [
      TimelinePlugin.create({
        timeInterval: 0.1,
        primaryLabelInterval: 1,
        formatTimeCallback: formatTimeline,
      }),
      ZoomPlugin.create({
        scale: 0.1,
        maxZoom: 500,
      }),
      HoverPlugin.create({
        lineColor: mediaOptions.cursorColor,
        lineWidth: mediaOptions.cursorWidth,
        formatTimeCallback: formatTime,
        labelColor: mediaOptions.waveColor as string,
      }),
    ], []),
    media: videoRef.current ?? undefined,
    mediaControls: false,
  })
  useEffect(() => {
    console.info('init wavesurfer')
    setProjectId(projectId as uuid)
    wavesurfer && wavesurfer.registerPlugin(regionsPlugin)
  }, [])
  useEffect(() => {
    return () => {
      console.info('destroy wavesurfer')
      setProjectId(undefined)
      if (videoRef !== null && videoRef.current !== null) {
        videoRef.current.preload = 'none'
      }
      if (wavesurfer) {
        regionsPlugin.destroy()
        wavesurfer.destroy()
      }
    }
  }, [])
  const handleLoadedMetadata = (event: React.SyntheticEvent<HTMLVideoElement, Event>) => {
    const height = event.currentTarget.videoHeight
    const width = event.currentTarget.videoWidth
  }
  const onPlayPause = useCallback(() => {
    wavesurfer && wavesurfer.playPause()
  }, [wavesurfer])
  const handleSilenceDetection = useCallback(() => {
    if (wavesurfer === null) return
    const decodeData = wavesurfer.getDecodedData()
    if (decodeData === null || decodeData.length === 0) return
    const channelData = decodeData.getChannelData(0)
    const regions = []
    const minValue = 0.01
    const minSilenceDuration = 0.1
    const mergeDuration = 0.2
    const scale = decodeData.duration / channelData.length
    const silentRegions = []

    // Find all silent regions longer than minSilenceDuration
    let start = 0
    let end = 0
    let isSilent = false
    for (let i = 0; i < channelData.length; i++) {
      if (channelData[i] < minValue) {
        if (!isSilent) {
          start = i
          isSilent = true
        }
      } else if (isSilent) {
        end = i
        isSilent = false
        if (scale * (end - start) > minSilenceDuration) {
          silentRegions.push({
            start: scale * start,
            end: scale * end,
          })
        }
      }
    }

    // Merge silent regions that are close together
    const mergedRegions = []
    let lastRegion = null
    for (let i = 0; i < silentRegions.length; i++) {
      if (lastRegion && silentRegions[i].start - lastRegion.end < mergeDuration) {
        lastRegion.end = silentRegions[i].end
      } else {
        lastRegion = silentRegions[i]
        mergedRegions.push(lastRegion)
      }
    }

    // Find regions that are not silent
    let lastEnd = 0
    for (let i = 0; i < mergedRegions.length; i++) {
      regions.push({
        start: lastEnd,
        end: mergedRegions[i].start,
      })
      lastEnd = mergedRegions[i].end
    }
  }, [wavesurfer])

  return (
    <Grid container sx={{ maxHeight: '100vh' }}>
      <Grid
        size={5.5}
        sx={{
          bgcolor: purple['100'],
          height: '70vh',
          display: 'flex',
          alignItems: 'center',
        }}
      >
        <Box
          sx={{
            position: 'relative',
            width: '100%',
          }}
        >
          <video
            src={`${import.meta.env.VITE_MEDIA_URL}/${projectId}/test.mp4`}
            ref={videoRef}
            disablePictureInPicture
            width={'100%'}
            height={'auto'}
            onLoadedMetadata={handleLoadedMetadata}
            crossOrigin="anonymous"
          />
        </Box>
      </Grid>
      <Grid size={6.5} sx={{
        bgcolor: purple['200'],
        height: '70vh',
      }}>
      </Grid>
      <Grid size={12} sx={{ width: '100%' }}>
        <Box id="wavesurfer" ref={wavesurferRef} component="div" />
      </Grid>
      <button onClick={onPlayPause} style={{ minWidth: '5em' }}>
        {isPlaying ? 'Pause' : 'Play'}
      </button>
      <ButtonGroup>
        <Button>previous line</Button>
        <Button>next line</Button>
        <Button>play until the end of region</Button>
      </ButtonGroup>
    </Grid>
  )
}