Feature: New Patient

  New Patient can be created

  Background:
    Given this api endpoint http://localhost:8080
    Given the following patient information
      | Jonce Lee | Male | 2000-10-19 | 200 Kern Street | 888 888 8888 |
      | Jenna Lee | Female | 2001-10-19 | 201 Kern Street | 887 888 8888 |

  Scenario: Adding Patient
    When I add Jonce Lee to the system
    And Get the patient list
    Then Jonce Lee is in the list

  Scenario: Updating patient
    When I update Jonce Lee in the system with Jenna Lee's information
    And Get the patient list after updating
    Then Jonce Lee's information in the new list is updated with Jenna Lee

  Scenario: Delete patient
    When I delete Jonce Lee in the system
    And Get the patient list after deleting
    Then Jonce Lee's information in the new list is gone