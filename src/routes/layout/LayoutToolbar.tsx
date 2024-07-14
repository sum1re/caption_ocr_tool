import { ListItem, ListItemButton, ListItemText, Menu, Stack, Toolbar } from '@mui/material';
import React, { useCallback, useMemo, useState } from 'react';
import { useHotkeys } from 'react-hotkeys-hook';
import { type LayoutMenuItem, LayoutToolbarMenuList } from './LayoutToolbarMenuList.tsx';

export function LayoutToolbar(): React.ReactElement {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };
  const activatedButton = useCallback((id: string) => (Boolean(anchorEl) && anchorEl!!.getAttribute('id') === id), [anchorEl]);
  const handleClose = () => {
    setAnchorEl(null);
  };
  const handleCreateProject = useCallback(() => {
    handleClose();
  }, []);
  const handleFileInfo = useCallback(() => {
    handleClose();
  }, []);
  const handleSettings = useCallback(() => {
    handleClose();
  }, []);
  useHotkeys('ctrl+alt+n', handleCreateProject);
  useHotkeys('ctrl+alt+s', handleSettings);
  useHotkeys('ctrl+d', handleFileInfo);
  const fileMenuItems = useMemo<LayoutMenuItem[]>(() => ([
    { label: 'New Project', shortcut: 'Ctrl + Alt + N', action: handleCreateProject },
    { label: 'Open Video', action: handleClose },
    'divider',
    { label: 'Export Ass', action: handleClose },
    { label: 'Export Srt', action: handleClose },
    { label: 'Export Image', action: handleClose },
    'divider',
    { label: 'Settings', shortcut: 'Ctrl + Alt + S', action: handleSettings },
    'divider',
    { label: 'File Info', shortcut: 'Ctrl + D', action: handleFileInfo },
    //{ label: 'Batch', action: handleClose },
    'divider',
    { label: 'Close Project', action: handleClose },
  ]), []);
  const helpMenuItems = useMemo<LayoutMenuItem[]>(() => ([
    { label: 'Help', action: handleClose },
    { label: 'About', action: handleClose },
  ]), []);
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
        {activatedButton('toolbar-file') && <LayoutToolbarMenuList items={fileMenuItems} width={220} />}
        {activatedButton('toolbar-help') && <LayoutToolbarMenuList items={helpMenuItems} width={180} />}
      </Menu>
    </Toolbar>
  );
}
