import React, { useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntity } from './publisher.reducer';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface IPublisherDetailProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const PublisherDetail = (props: IPublisherDetailProps) => {
  useEffect(() => {
    props.getEntity(props.match.params.id);
  }, []);

  const { publisherEntity } = props;
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="publisherDetailsHeading">
          <Translate contentKey="libraryAppKotlinApp.publisher.detail.title">Publisher</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{publisherEntity.id}</dd>
          <dt>
            <span id="name">
              <Translate contentKey="libraryAppKotlinApp.publisher.name">Name</Translate>
            </span>
          </dt>
          <dd>{publisherEntity.name}</dd>
        </dl>
        <Button tag={Link} to="/publisher" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/publisher/${publisherEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

const mapStateToProps = ({ publisher }: IRootState) => ({
  publisherEntity: publisher.entity,
});

const mapDispatchToProps = { getEntity };

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(PublisherDetail);
