import { Divider, ListItemText, MenuItem, MenuList, Typography } from '@mui/material';
import React from 'react';

export type LayoutMenuItem = {
  label: string,
  shortcut?: string,
  action: () => void,
} | 'divider'

type LayoutToolbarMenuListProps = {
  items: LayoutMenuItem[];
  width: number
}

export function LayoutToolbarMenuList({ width, items }: LayoutToolbarMenuListProps): React.ReactElement {
  return (
    <MenuList dense sx={{ width, maxWidth: '100%' }}>
      {items.map((item, index) => {
        if (typeof item === 'string') {
          return (<Divider key={index} />);
        }
        return (
          <MenuItem onClick={item.action} key={index}>
            <ListItemText>{item.label}</ListItemText>
            {item.shortcut && <Typography variant="body2" color="text.secondary">{item.shortcut}</Typography>}
          </MenuItem>
        );
      })}
    </MenuList>
  );
}
