*** Settings ***
Documentation  Verify that API is working correctly.

Resource  common.robot

Test Setup  Initialize
Test Teardown  Delete All Sessions

*** Variables ***

*** Test Cases ***

Check API
  [Tags]  API  Requirement
  Check API path   validate/rules
# Check API path   validate/result/100

*** Keywords ***

Check API path
  [Arguments]  ${path}  ${q}=${EMPTY}
  &{params}  Create Dictionary  q=${q}
  Get API Request  ${URL_CTX}/${path}  ${params}
  Get API Request Should Have Returned JSON

