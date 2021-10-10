import React from 'react';
import MenuItem from 'app/shared/layout/menus/menu-item';
import { Translate, translate } from 'react-jhipster';
import { NavDropdown } from './menu-components';

export const EntitiesMenu = props => (
  <NavDropdown
    icon="th-list"
    name={translate('global.menu.entities.main')}
    id="entity-menu"
    data-cy="entity"
    style={{ maxHeight: '80vh', overflow: 'auto' }}
  >
    <MenuItem icon="asterisk" to="/publisher">
      <Translate contentKey="global.menu.entities.publisher" />
    </MenuItem>
    <MenuItem icon="asterisk" to="/author">
      <Translate contentKey="global.menu.entities.author" />
    </MenuItem>
    <MenuItem icon="asterisk" to="/client">
      <Translate contentKey="global.menu.entities.client" />
    </MenuItem>
    <MenuItem icon="asterisk" to="/book">
      <Translate contentKey="global.menu.entities.book" />
    </MenuItem>
    <MenuItem icon="asterisk" to="/borrowed-book">
      <Translate contentKey="global.menu.entities.borrowedBook" />
    </MenuItem>
    {/* jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here */}
  </NavDropdown>
);
