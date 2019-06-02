library TestFhirExecution version '1.0'

using FHIR version '3.0.0'

context Patient

define "Test AgeInYears":
  AgeInYears() >= 18