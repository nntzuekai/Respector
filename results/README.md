This directory contains the folders containing precomputed results and the OpenAPI specifications from which they are computed for the following research questions.

1. RQ1: Can Respector generate accurate specifications?
For the APIs we considered, Respector generated specifications with, on average, 100% precision and 98.2% recall in inferring endpoint methods,  100% precision and 94.4% recall in inferring endpoint parameters,	100% precision and 92.6% recall in inferring responses, and 92.6% precision and 50.0% recall in inferring parameter constraints. Further, Respector accurately detected a total of 4,806 inter-dependencies across 100 endpoint methods.

2. RQ2: How do Respector-generated specifications compare with developer-provided specifications?
For the APIs we evaluated, the Respector-generated specifications contained 228 endpoint methods, 2,795 parameters, 15 constraints, and 502 responses missing from the developer-provided specifications.
Respector also identified 4 constraints that were inconsistent with the developer-provided specifications and were confirmed by the developers.

3. RQ3: How does Respector compare with alternative state-of-the-art API specification generation techniques?
For the APIs we considered, four existing techniques (AppMap, Swagger Core, springdoc-openapi , and SpringFox) failed to detect all specification components detected by Respector. The techniques detected only on average, 75.43% endpoint methods, 41.85% parameters, 18.35% constraints, and 63.97% responses detected by Respector.

