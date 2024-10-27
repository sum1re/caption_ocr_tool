import { CloudUpload as CloudUploadIcon } from '@mui/icons-material'
import { Box, Button, styled } from '@mui/material'
import { useAtomValue } from 'jotai'
import { isEmpty } from 'moderndash'
import React, { type ChangeEvent } from 'react'
import { Link } from 'react-router-dom'
import { useFetchProjects } from '../../api/project/use-fetch-projects'
import { projectIdAtom } from '../../provider/atom-provider'
import { useUploadFile } from '../../utils/hooks'

type WelcomeProps = {};

const VisuallyHiddenInput = styled('input')({
  width: 1,
  height: 1,
  overflow: 'hidden',
  position: 'absolute',
  bottom: 0,
  left: 0,
  whiteSpace: 'nowrap',
})

//TODO(no design)
export function Welcome({}: WelcomeProps): React.ReactElement {
  const projectId = useAtomValue(projectIdAtom)
  const { data: projects } = useFetchProjects()
  const { uploadFile } = useUploadFile()

  const handleNewClick = () => {
    //TODO
  }
  const handleChange = async (event: ChangeEvent<HTMLInputElement>) => {
    const files = event.target.files
    if (files === null || files.length === 0) return
    uploadFile({ file: files[0], projectId })
  }
  return (
    <Box>
      <Button onClick={handleNewClick}>New Project</Button>
      {isEmpty(projects) ? (<div>Empty List</div>) : (<Box>
        {projects!!.map(({ id, name }) => (
          <Button
            key={id}
            component={Link}
            to={`/${id}`}
          >
            {id} - {name}
          </Button>
        ))}
      </Box>)}
      <Button
        component="label"
        variant="contained"
        tabIndex={-1}
        startIcon={<CloudUploadIcon />}
      >
        Upload file
        <VisuallyHiddenInput
          type="file"
          onChange={handleChange}
        />
      </Button>
    </Box>
  )
}