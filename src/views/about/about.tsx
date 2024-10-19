import { Box, Button, Typography } from '@mui/material'
import React from 'react'
import { Link } from 'react-router-dom'
import { useFetchInfo } from '../../api/app/use-fetch-info'

//TODO
export function About(): React.ReactElement {
  const { data } = useFetchInfo()
  return (
    <Box sx={{
      mx: 'auto',
      width: '20vw',
    }}>
      <Typography variant="h1" gutterBottom>App info</Typography>
      <Typography variant="h3" gutterBottom>{data?.name}</Typography>
      <Typography variant="h5" gutterBottom>{data?.appLicense}</Typography>
      <Typography variant="body2" gutterBottom>{data?.version}</Typography>
      <Typography variant="body2" gutterBottom>{data?.buildTimestamp}</Typography>
      <Button component={Link} to="/">Back to Home</Button>
    </Box>
  )
}