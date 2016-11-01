#! /bin/sh
set -eux

raml2html -i self-assessment-api-granularity-level-1-v1.raml -o self-assessment-api-granularity-level-1-v1.html
raml2html -i self-assessment-api-granularity-level-2-v1.raml -o self-assessment-api-granularity-level-2-v1.html
raml2html -i self-assessment-api-granularity-level-3-v1.raml -o self-assessment-api-granularity-level-3-v1.html
raml2html -i self-assessment-api-granularity-level-3-v2.raml -o self-assessment-api-granularity-level-3-v2.html
