import { Divider, ListItemText, MenuItem, MenuList, Typography } from '@mui/material'
import React from 'react'

export type LayoutMenuItem = {
  label: string,
  shortcut?: string,
  action: () => void,
  disabled?: boolean,
} | 'divider'

type LayoutMenuProps = {
  items: LayoutMenuItem[];
  width: number;
  onClose: () => void;
}

export function LayoutMenu({ width, items, onClose }: LayoutMenuProps): React.ReactElement {
  return (
    <MenuList dense sx={{ width, maxWidth: '100%' }}>
      {items.map((item, index) => {
        if (typeof item === 'string') {
          return (<Divider key={index} />)
        }
        return (
          <MenuItem
            onClick={() => {
              item.action()
              onClose()
            }}
            key={index}
            disabled={item.disabled}
          >
            <ListItemText>{item.label}</ListItemText>
            {item.shortcut && <Typography variant="body2" color="text.secondary">{item.shortcut}</Typography>}
          </MenuItem>
        )
      })}
    </MenuList>
  )
}
