

To convert to Swagger 2.0:

  npm i -g api-spec-converter
  npm i -g json2yaml
  api-spec-converter -f raml -t swagger_2 -d self-assessment-api-granularity-level-2-v1.raml | json2yaml > self-assessment-api-granularity-level-2-v1.yml
