    const getRandomString = (length) => {
      let randomChars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz';
      let result = '';
      for ( let i = 0; i < length; i++ ) {
          result += randomChars.charAt(Math.floor(Math.random() * randomChars.length));
      }
      return result;
    }

describe('template spec', () => {
  it('passes', () => {
      cy.intercept('POST', '/api/patients').as('newPatient')
      cy.visit('http://localhost:8700')

      // create patient
      cy.contains('New Patient').click()
      const inputName = getRandomString(6)
      cy.get('#patient-form-name').type(inputName)
      cy.get('#patient-form-gender').select('Male')
      cy.get('#patient-form-dob').type('2000-12-27')
      cy.get('#patient-form-address').type('3000 Chestnut Ave, Mesa, CA 93345')
      cy.get('#patient-form-phone').type('333 444 5555')
      cy.contains('Submit').click()
      cy.wait('@newPatient').its('response.statusCode').should('eq', 200)
      cy.get('#success-message').should('be.visible').and('contain', 'Request Success')
      cy.contains('Dismiss').click()

      // isolate patient to update it
      cy.get('#search-input').type(inputName)

      // assert entered values

      //update patient
      cy.intercept('PUT', '/api/patients/*').as('updatePatient')
      cy.contains('Edit').click()

      const updateName = getRandomString(6)
      cy.get('#patient-form-name').clear()
      cy.get('#patient-form-name').type(updateName)
      cy.get('#patient-form-gender').select('Female')
      cy.get('#patient-form-dob').clear()
      cy.get('#patient-form-dob').type('1990-12-27')
      cy.get('#patient-form-address').clear()
      cy.get('#patient-form-address').type('4000 Walnut Ave, Mesa, CA 93345')
      cy.get('#patient-form-phone').clear()
      cy.get('#patient-form-phone').type('666 444 5555')
      cy.contains('Submit').click()

      cy.wait('@updatePatient').its('response.statusCode').should('eq', 200)
      cy.get('#success-message').should('be.visible').and('contain', 'Request Success')
      cy.contains('Dismiss').click()

      // locate patient by updated name and delete patient
      cy.intercept('DELETE', '/api/patients/*').as('deletePatient')
      cy.get('#search-input').clear()
      cy.get('#search-input').type(updateName)
      cy.get('#check-one').check()
      cy.contains('Delete Selected').click()
      cy.wait('@deletePatient').its('response.statusCode').should('eq', 200)
      cy.get('#success-message').should('be.visible').and('contain', 'Request Success')


  })
})
