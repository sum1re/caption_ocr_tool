import { ListItem, ListItemButton, ListItemText, Menu, Stack, Toolbar } from '@mui/material'
import { useAtomValue } from 'jotai'
import React, { useCallback, useMemo, useState } from 'react'
import { useHotkeys } from 'react-hotkeys-hook'
import { useNavigate } from 'react-router-dom'
import { projectIdAtom } from '../../provider/atom-provider'
import { LayoutMenu, type LayoutMenuItem } from './layout-menu.tsx'

export function LayoutToolbar(): React.ReactElement {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null)
  const projectId = useAtomValue(projectIdAtom)
  const navigate = useNavigate()
  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget)
  }
  const activatedButton = useCallback((id: string) => (Boolean(anchorEl) && anchorEl!!.getAttribute('id') === id), [anchorEl])
  const handleClose = () => {
    setAnchorEl(null)
  }
  const handleCreateProject = useCallback(() => {
    handleClose()
  }, [])
  const handleFileInfo = useCallback(() => {
    handleClose()
  }, [])
  const handleSettings = useCallback(() => {
    handleClose()
  }, [])
  useHotkeys('ctrl+alt+n', handleCreateProject)
  useHotkeys('ctrl+alt+s', handleSettings)
  useHotkeys('ctrl+d', handleFileInfo)
  const fileMenuItems = useMemo<LayoutMenuItem[]>(() => ([
    { label: 'New Project', shortcut: 'Ctrl + Alt + N', action: handleCreateProject },
    { label: 'Open Video', action: handleClose },
    'divider',
    { label: 'Export Ass', action: handleClose, disabled: projectId === undefined },
    { label: 'Export Srt', action: handleClose, disabled: projectId === undefined },
    { label: 'Export Image', action: handleClose, disabled: projectId === undefined },
    'divider',
    {
      label: 'Settings',
      shortcut: 'Ctrl + Alt + S',
      action: () => {
        navigate('/settings')
      },
    },
    'divider',
    { label: 'File Info', shortcut: 'Ctrl + D', action: handleFileInfo, disabled: projectId === undefined },
    //{ label: 'Batch', action: handleClose },
    'divider',
    {
      label: 'Close Project',
      action: () => {
        navigate('/')
      },
      disabled: projectId === undefined,
    },
    {
      label: 'Back to Home',
      action: () => {
        navigate('/')
      },
    },
  ]), [projectId])
  const filterMenuItems = useMemo<LayoutMenuItem[]>(() => ([
    {
      label: 'Create Filter',
      action: () => {
        navigate(`/${projectId}/filter`)
      },
      disabled: projectId === undefined,
    },
  ]), [projectId])
  const helpMenuItems = useMemo<LayoutMenuItem[]>(() => ([
    {
      label: 'Help',
      action: () => {
        window.open('https://github.com/sum1re/caption_ocr_tool/blob/main/README.MD', '_blank')
      },
    },
    {
      label: 'About',
      action: () => {
        navigate('/about')
      },
    },
  ]), [])
  return (
    <Toolbar variant="dense">
      <Stack spacing={1} direction="row">
        <ListItem sx={{ p: 0 }}>
          <ListItemButton
            id="toolbar-file"
            onClick={handleClick}
          >
            <ListItemText>File</ListItemText>
          </ListItemButton>
        </ListItem>
        <ListItem sx={{ p: 0 }}>
          <ListItemButton
            id="toolbar-filter"
            onClick={handleClick}
          >
            <ListItemText>Filter</ListItemText>
          </ListItemButton>
        </ListItem>
        <ListItem sx={{ p: 0 }}>
          <ListItemButton
            id="toolbar-help"
            onClick={handleClick}
          >
            <ListItemText>Help</ListItemText>
          </ListItemButton>
        </ListItem>
      </Stack>
      <Menu
        id="toolbar-menu"
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleClose}
        transitionDuration={0}
      >
        {activatedButton('toolbar-file') && <LayoutMenu items={fileMenuItems} width={220} onClose={handleClose} />}
        {activatedButton('toolbar-filter') && <LayoutMenu items={filterMenuItems} width={220} onClose={handleClose} />}
        {activatedButton('toolbar-help') && <LayoutMenu items={helpMenuItems} width={180} onClose={handleClose} />}
      </Menu>
    </Toolbar>
  )
}
