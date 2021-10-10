import React, { useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntity } from './client.reducer';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface IClientDetailProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const ClientDetail = (props: IClientDetailProps) => {
  useEffect(() => {
    props.getEntity(props.match.params.id);
  }, []);

  const { clientEntity } = props;
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="clientDetailsHeading">
          <Translate contentKey="libraryAppKotlinApp.client.detail.title">Client</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{clientEntity.id}</dd>
          <dt>
            <span id="firstName">
              <Translate contentKey="libraryAppKotlinApp.client.firstName">First Name</Translate>
            </span>
          </dt>
          <dd>{clientEntity.firstName}</dd>
          <dt>
            <span id="lastName">
              <Translate contentKey="libraryAppKotlinApp.client.lastName">Last Name</Translate>
            </span>
          </dt>
          <dd>{clientEntity.lastName}</dd>
          <dt>
            <span id="email">
              <Translate contentKey="libraryAppKotlinApp.client.email">Email</Translate>
            </span>
          </dt>
          <dd>{clientEntity.email}</dd>
          <dt>
            <span id="address">
              <Translate contentKey="libraryAppKotlinApp.client.address">Address</Translate>
            </span>
          </dt>
          <dd>{clientEntity.address}</dd>
          <dt>
            <span id="phone">
              <Translate contentKey="libraryAppKotlinApp.client.phone">Phone</Translate>
            </span>
          </dt>
          <dd>{clientEntity.phone}</dd>
        </dl>
        <Button tag={Link} to="/client" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/client/${clientEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

const mapStateToProps = ({ client }: IRootState) => ({
  clientEntity: client.entity,
});

const mapDispatchToProps = { getEntity };

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(ClientDetail);
