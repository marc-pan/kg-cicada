#!/bin/sh
if [ $# -ne 1 ]
then
        echo "usage:$(basename $0) bucket_name,for example $(basename $0) yxu-bucket1"

        exit -1
fi
bucket_name=$1

#dlfvt_object_storage_instance_apikey
apikey=xL1C02t9COXe2fNjJi9_j6X28l5lVi8o8RAMAvvavvJT


TK=$(curl "https://iam.test.cloud.ibm.com/identity/token" \
-d "grant_type=urn:ibm:params:oauth:grant-type:apikey&response_type=cloud_iam&apikey=${apikey}" \
-H "Content-Type: application/x-www-form-urlencoded" \
-H "Accept: application/json" | awk -F , '{print $1}' | awk -F : '{print $2}')
TK=${TK#?}
TK=${TK%?}



body='<CreateBucketConfiguration xmlns="http://s3.amazonaws.com/doc/2006-03-01/"> <LocationConstraint>eu-standard</LocationConstraint> </CreateBucketConfiguration>'

dlfvt_service_instance_id="a9dfd8d1-d91b-4486-ab7f-5f6232c9f2d6"
storage_endpoint_bluemix_cos="https://s3.us-west.cloud-object-storage.test.appdomain.cloud"
storage_host_bluemix_cos="s3.us-west.cloud-object-storage.test.appdomain.cloud"

curl -X PUT "${storage_endpoint_bluemix_cos}/${bucket_name}" \
  --header "host: ${storage_host_bluemix_cos}" \
  --header 'Content-Type: text/plain' \
  --header "ibm-service-instance-id: ${dlfvt_service_instance_id}" \
  --header "Authorization: Bearer $TK" \
  --data-raw '<CreateBucketConfiguration xmlns="http://s3.amazonaws.com/doc/2006-03-01/"> <LocationConstraint>eu-standard</LocationConstraint> </CreateBucketConfiguration>'
