This directory contains some code related to the MARC4J project.

ValidatorFilter is a SAX2 XMLFilterImpl subclass that enables programs to validate XML documents against a RELAX NG schema using Jing within a SAX2 pipeline.

ValidatorHandler is a driver to test the ValidatorFilter.

ValidatorFilter is not scheduled to be part of MARC4J, since it depends on the Jing validator.
Currently there is no Validator interface in JAXP that provides handlers or filters to be used in SAX2 pipelines.

Bas Peters
mail@bpeters.com