#!/bin/env bash

# Case 1
ret=$(pactester -p ./proxy.pac -u https://www.ibm.com)
printf "%s: %s\n" "Case 1" $([[ ${ret} == "DIRECT" ]] && echo pass || echo failure)

# Case 2
ret=$(pactester -p ./proxy.pac -u https://www.x.com)
printf "%s: %s\n" "Case 2" $([[ ${ret} != "DIRECT" ]] && echo pass || echo failure)
