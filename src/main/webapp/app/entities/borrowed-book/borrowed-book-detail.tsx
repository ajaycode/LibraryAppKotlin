import React, { useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntity } from './borrowed-book.reducer';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface IBorrowedBookDetailProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const BorrowedBookDetail = (props: IBorrowedBookDetailProps) => {
  useEffect(() => {
    props.getEntity(props.match.params.id);
  }, []);

  const { borrowedBookEntity } = props;
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="borrowedBookDetailsHeading">
          <Translate contentKey="libraryAppKotlinApp.borrowedBook.detail.title">BorrowedBook</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{borrowedBookEntity.id}</dd>
          <dt>
            <span id="borrowDate">
              <Translate contentKey="libraryAppKotlinApp.borrowedBook.borrowDate">Borrow Date</Translate>
            </span>
          </dt>
          <dd>
            {borrowedBookEntity.borrowDate ? (
              <TextFormat value={borrowedBookEntity.borrowDate} type="date" format={APP_LOCAL_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <Translate contentKey="libraryAppKotlinApp.borrowedBook.book">Book</Translate>
          </dt>
          <dd>{borrowedBookEntity.book ? borrowedBookEntity.book.name : ''}</dd>
          <dt>
            <Translate contentKey="libraryAppKotlinApp.borrowedBook.client">Client</Translate>
          </dt>
          <dd>{borrowedBookEntity.client ? borrowedBookEntity.client.email : ''}</dd>
        </dl>
        <Button tag={Link} to="/borrowed-book" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/borrowed-book/${borrowedBookEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

const mapStateToProps = ({ borrowedBook }: IRootState) => ({
  borrowedBookEntity: borrowedBook.entity,
});

const mapDispatchToProps = { getEntity };

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(BorrowedBookDetail);
