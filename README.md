# CelestaDoc

Generates celesta documentation report.

First, you should install project:

```mvn clean install```

After you can get zip-archive CelestaDoc, inside it you can find celestadoc.bat file.
Run it with the next command:

```celestadoc.bat <path_to_directory_with_celesta_sql> <prefix> <output_file.adoc> [optional]-pdf [optional]-html```

where ```path_to_directory_with_celesta_sql``` - directory with celesta sql,

```prefix``` - some prefix which is used in celesta docs (For example, ```doc-ru```)

```output_file.adoc``` - path to file which will be created. It should be .adoc file

```-pdf``` - optional flag for generating pdf file from adoc file. This file will be
created in the same directory as .adoc report, and it will had the same name ```output_file.pdf```

```-html``` - optional flag for generating html file from adoc file. This file will be created in the same directory
as .adoc report, and it will had the same name ```output_file.html```
