# How to run `fvt.py` to create more business terms in CPD

1. install `requests` module

    ```bash
    pip3 install -r scripts/requirements.txt
    ```

2. run following command in parent directory of scripts directory

    ```bash
    python3 scripts/fvt.py --username admin --password password --loops 12
    ```

Notes:

- open `scripts/environments.ini` file and fill in the value of `API_SERVER_URI` in `cpd450` section

  ```ini
  [cpd450]
  API_SERVER_URI = cpd-wkc.apps.cpd450staging.cp.fyre.ibm.com
  ```

- `--username/--password` is WKC username/password who should have right permission/role to create category and term
- it will create 3 terms in each loop, so if `loops=12` there will be 36 terms to be created in total
- new term name starts with `FVT_ERGO`


As for `portal-job-manager` service, start the service by `DEPLOYMENT_TARGET=icp node ./index.js` command to running in private cloud environment.


