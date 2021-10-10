import {
  entityTableSelector,
  entityDetailsButtonSelector,
  entityDetailsBackButtonSelector,
  entityCreateButtonSelector,
  entityCreateSaveButtonSelector,
  entityEditButtonSelector,
  entityDeleteButtonSelector,
  entityConfirmDeleteButtonSelector,
} from '../../support/entity';

describe('Book e2e test', () => {
  let startingEntitiesCount = 0;

  before(() => {
    cy.window().then(win => {
      win.sessionStorage.clear();
    });
    cy.clearCookie('SESSION');
    cy.clearCookies();
    cy.intercept('GET', '/api/books*').as('entitiesRequest');
    cy.visit('');
    cy.login('admin', 'admin');
    cy.clickOnEntityMenuItem('book');
    cy.wait('@entitiesRequest').then(({ request, response }) => (startingEntitiesCount = response.body.length));
    cy.visit('/');
  });

  it('should load Books', () => {
    cy.intercept('GET', '/api/books*').as('entitiesRequest');
    cy.visit('/');
    cy.clickOnEntityMenuItem('book');
    cy.wait('@entitiesRequest');
    cy.getEntityHeading('Book').should('exist');
    if (startingEntitiesCount === 0) {
      cy.get(entityTableSelector).should('not.exist');
    } else {
      cy.get(entityTableSelector).should('have.lengthOf', startingEntitiesCount);
    }
    cy.visit('/');
  });

  it('should load details Book page', () => {
    cy.intercept('GET', '/api/books*').as('entitiesRequest');
    cy.visit('/');
    cy.clickOnEntityMenuItem('book');
    cy.wait('@entitiesRequest');
    if (startingEntitiesCount > 0) {
      cy.get(entityDetailsButtonSelector).first().click({ force: true });
      cy.getEntityDetailsHeading('book');
      cy.get(entityDetailsBackButtonSelector).should('exist');
    }
    cy.visit('/');
  });

  it('should load create Book page', () => {
    cy.intercept('GET', '/api/books*').as('entitiesRequest');
    cy.visit('/');
    cy.clickOnEntityMenuItem('book');
    cy.wait('@entitiesRequest');
    cy.get(entityCreateButtonSelector).click({ force: true });
    cy.getEntityCreateUpdateHeading('Book');
    cy.get(entityCreateSaveButtonSelector).should('exist');
    cy.visit('/');
  });

  it('should load edit Book page', () => {
    cy.intercept('GET', '/api/books*').as('entitiesRequest');
    cy.visit('/');
    cy.clickOnEntityMenuItem('book');
    cy.wait('@entitiesRequest');
    if (startingEntitiesCount > 0) {
      cy.get(entityEditButtonSelector).first().click({ force: true });
      cy.getEntityCreateUpdateHeading('Book');
      cy.get(entityCreateSaveButtonSelector).should('exist');
    }
    cy.visit('/');
  });

  it('should create an instance of Book', () => {
    cy.intercept('GET', '/api/books*').as('entitiesRequest');
    cy.visit('/');
    cy.clickOnEntityMenuItem('book');
    cy.wait('@entitiesRequest').then(({ request, response }) => (startingEntitiesCount = response.body.length));
    cy.get(entityCreateButtonSelector).click({ force: true });
    cy.getEntityCreateUpdateHeading('Book');

    cy.get(`[data-cy="isbn"]`).type('leverage mult', { force: true }).invoke('val').should('match', new RegExp('leverage mult'));

    cy.get(`[data-cy="name"]`).type('deposit', { force: true }).invoke('val').should('match', new RegExp('deposit'));

    cy.get(`[data-cy="publishYear"]`).type('optimize', { force: true }).invoke('val').should('match', new RegExp('optimize'));

    cy.get(`[data-cy="copies"]`).type('13053').should('have.value', '13053');

    cy.setFieldImageAsBytesOfEntity('cover', 'integration-test.png', 'image/png');

    cy.setFieldSelectToLastOfEntity('publisher');

    cy.setFieldSelectToLastOfEntity('author');

    cy.get(entityCreateSaveButtonSelector).click({ force: true });
    cy.scrollTo('top', { ensureScrollable: false });
    cy.get(entityCreateSaveButtonSelector).should('not.exist');
    cy.intercept('GET', '/api/books*').as('entitiesRequestAfterCreate');
    cy.visit('/');
    cy.clickOnEntityMenuItem('book');
    cy.wait('@entitiesRequestAfterCreate');
    cy.get(entityTableSelector).should('have.lengthOf', startingEntitiesCount + 1);
    cy.visit('/');
  });

  it('should delete last instance of Book', () => {
    cy.intercept('GET', '/api/books*').as('entitiesRequest');
    cy.intercept('GET', '/api/books/*').as('dialogDeleteRequest');
    cy.intercept('DELETE', '/api/books/*').as('deleteEntityRequest');
    cy.visit('/');
    cy.clickOnEntityMenuItem('book');
    cy.wait('@entitiesRequest').then(({ request, response }) => {
      startingEntitiesCount = response.body.length;
      if (startingEntitiesCount > 0) {
        cy.get(entityTableSelector).should('have.lengthOf', startingEntitiesCount);
        cy.get(entityDeleteButtonSelector).last().click({ force: true });
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('book').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click({ force: true });
        cy.wait('@deleteEntityRequest');
        cy.intercept('GET', '/api/books*').as('entitiesRequestAfterDelete');
        cy.visit('/');
        cy.clickOnEntityMenuItem('book');
        cy.wait('@entitiesRequestAfterDelete');
        cy.get(entityTableSelector).should('have.lengthOf', startingEntitiesCount - 1);
      }
      cy.visit('/');
    });
  });
});
