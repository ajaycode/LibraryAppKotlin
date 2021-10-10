import React, { useState, useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col, Label } from 'reactstrap';
import { AvFeedback, AvForm, AvGroup, AvInput, AvField } from 'availity-reactstrap-validation';
import { setFileData, openFile, byteSize, Translate, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { IRootState } from 'app/shared/reducers';

import { IPublisher } from 'app/shared/model/publisher.model';
import { getEntities as getPublishers } from 'app/entities/publisher/publisher.reducer';
import { IAuthor } from 'app/shared/model/author.model';
import { getEntities as getAuthors } from 'app/entities/author/author.reducer';
import { getEntity, updateEntity, createEntity, setBlob, reset } from './book.reducer';
import { IBook } from 'app/shared/model/book.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';

export interface IBookUpdateProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const BookUpdate = (props: IBookUpdateProps) => {
  const [idsauthor, setIdsauthor] = useState([]);
  const [isNew] = useState(!props.match.params || !props.match.params.id);

  const { bookEntity, publishers, authors, loading, updating } = props;

  const { cover, coverContentType } = bookEntity;

  const handleClose = () => {
    props.history.push('/book' + props.location.search);
  };

  useEffect(() => {
    if (isNew) {
      props.reset();
    } else {
      props.getEntity(props.match.params.id);
    }

    props.getPublishers();
    props.getAuthors();
  }, []);

  const onBlobChange = (isAnImage, name) => event => {
    setFileData(event, (contentType, data) => props.setBlob(name, data, contentType), isAnImage);
  };

  const clearBlob = name => () => {
    props.setBlob(name, undefined, undefined);
  };

  useEffect(() => {
    if (props.updateSuccess) {
      handleClose();
    }
  }, [props.updateSuccess]);

  const saveEntity = (event, errors, values) => {
    if (errors.length === 0) {
      const entity = {
        ...bookEntity,
        ...values,
        authors: mapIdList(values.authors),
        publisher: publishers.find(it => it.id.toString() === values.publisherId.toString()),
      };

      if (isNew) {
        props.createEntity(entity);
      } else {
        props.updateEntity(entity);
      }
    }
  };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="libraryAppKotlinApp.book.home.createOrEditLabel" data-cy="BookCreateUpdateHeading">
            <Translate contentKey="libraryAppKotlinApp.book.home.createOrEditLabel">Create or edit a Book</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <AvForm model={isNew ? {} : bookEntity} onSubmit={saveEntity}>
              {!isNew ? (
                <AvGroup>
                  <Label for="book-id">
                    <Translate contentKey="global.field.id">ID</Translate>
                  </Label>
                  <AvInput id="book-id" type="text" className="form-control" name="id" required readOnly />
                </AvGroup>
              ) : null}
              <AvGroup>
                <Label id="isbnLabel" for="book-isbn">
                  <Translate contentKey="libraryAppKotlinApp.book.isbn">Isbn</Translate>
                </Label>
                <AvField
                  id="book-isbn"
                  data-cy="isbn"
                  type="text"
                  name="isbn"
                  validate={{
                    required: { value: true, errorMessage: translate('entity.validation.required') },
                    minLength: { value: 5, errorMessage: translate('entity.validation.minlength', { min: 5 }) },
                    maxLength: { value: 13, errorMessage: translate('entity.validation.maxlength', { max: 13 }) },
                  }}
                />
              </AvGroup>
              <AvGroup>
                <Label id="nameLabel" for="book-name">
                  <Translate contentKey="libraryAppKotlinApp.book.name">Name</Translate>
                </Label>
                <AvField
                  id="book-name"
                  data-cy="name"
                  type="text"
                  name="name"
                  validate={{
                    required: { value: true, errorMessage: translate('entity.validation.required') },
                    maxLength: { value: 100, errorMessage: translate('entity.validation.maxlength', { max: 100 }) },
                  }}
                />
              </AvGroup>
              <AvGroup>
                <Label id="publishYearLabel" for="book-publishYear">
                  <Translate contentKey="libraryAppKotlinApp.book.publishYear">Publish Year</Translate>
                </Label>
                <AvField
                  id="book-publishYear"
                  data-cy="publishYear"
                  type="text"
                  name="publishYear"
                  validate={{
                    required: { value: true, errorMessage: translate('entity.validation.required') },
                    minLength: { value: 4, errorMessage: translate('entity.validation.minlength', { min: 4 }) },
                    maxLength: { value: 50, errorMessage: translate('entity.validation.maxlength', { max: 50 }) },
                  }}
                />
              </AvGroup>
              <AvGroup>
                <Label id="copiesLabel" for="book-copies">
                  <Translate contentKey="libraryAppKotlinApp.book.copies">Copies</Translate>
                </Label>
                <AvField
                  id="book-copies"
                  data-cy="copies"
                  type="string"
                  className="form-control"
                  name="copies"
                  validate={{
                    required: { value: true, errorMessage: translate('entity.validation.required') },
                    number: { value: true, errorMessage: translate('entity.validation.number') },
                  }}
                />
              </AvGroup>
              <AvGroup>
                <AvGroup>
                  <Label id="coverLabel" for="cover">
                    <Translate contentKey="libraryAppKotlinApp.book.cover">Cover</Translate>
                  </Label>
                  <br />
                  {cover ? (
                    <div>
                      {coverContentType ? (
                        <a onClick={openFile(coverContentType, cover)}>
                          <img src={`data:${coverContentType};base64,${cover}`} style={{ maxHeight: '100px' }} />
                        </a>
                      ) : null}
                      <br />
                      <Row>
                        <Col md="11">
                          <span>
                            {coverContentType}, {byteSize(cover)}
                          </span>
                        </Col>
                        <Col md="1">
                          <Button color="danger" onClick={clearBlob('cover')}>
                            <FontAwesomeIcon icon="times-circle" />
                          </Button>
                        </Col>
                      </Row>
                    </div>
                  ) : null}
                  <input id="file_cover" data-cy="cover" type="file" onChange={onBlobChange(true, 'cover')} accept="image/*" />
                  <AvInput type="hidden" name="cover" value={cover} />
                </AvGroup>
              </AvGroup>
              <AvGroup>
                <Label for="book-publisher">
                  <Translate contentKey="libraryAppKotlinApp.book.publisher">Publisher</Translate>
                </Label>
                <AvInput id="book-publisher" data-cy="publisher" type="select" className="form-control" name="publisherId">
                  <option value="" key="0" />
                  {publishers
                    ? publishers.map(otherEntity => (
                        <option value={otherEntity.id} key={otherEntity.id}>
                          {otherEntity.name}
                        </option>
                      ))
                    : null}
                </AvInput>
              </AvGroup>
              <AvGroup>
                <Label for="book-author">
                  <Translate contentKey="libraryAppKotlinApp.book.author">Author</Translate>
                </Label>
                <AvInput
                  id="book-author"
                  data-cy="author"
                  type="select"
                  multiple
                  className="form-control"
                  name="authors"
                  value={!isNew && bookEntity.authors && bookEntity.authors.map(e => e.id)}
                >
                  <option value="" key="0" />
                  {authors
                    ? authors.map(otherEntity => (
                        <option value={otherEntity.id} key={otherEntity.id}>
                          {otherEntity.firstName}
                        </option>
                      ))
                    : null}
                </AvInput>
              </AvGroup>
              <Button tag={Link} id="cancel-save" to="/book" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp;
                <Translate contentKey="entity.action.save">Save</Translate>
              </Button>
            </AvForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

const mapStateToProps = (storeState: IRootState) => ({
  publishers: storeState.publisher.entities,
  authors: storeState.author.entities,
  bookEntity: storeState.book.entity,
  loading: storeState.book.loading,
  updating: storeState.book.updating,
  updateSuccess: storeState.book.updateSuccess,
});

const mapDispatchToProps = {
  getPublishers,
  getAuthors,
  getEntity,
  updateEntity,
  setBlob,
  createEntity,
  reset,
};

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(BookUpdate);
