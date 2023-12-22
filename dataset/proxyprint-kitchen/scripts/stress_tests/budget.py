import requests
import random
import json
import numbers

# Global data configs
N_PRINTSHOPS=8
MAX_ALLOWED_BUDGETS=5
MAX_CONSUMERS=1000
# URL
url = "http://localhost:8080/consumer/budget"
# Files location and file selection
files_pool = "/home/daniel/Desktop/files_pool/"
file_name = str(random.randrange(1,17))+".pdf"
file_path = files_pool+file_name
# PrintShops random selection
printshops = []
printshops.append(8)
pshop_id = random.randrange(1,N_PRINTSHOPS)
for i in range (1,random.randrange(1,MAX_ALLOWED_BUDGETS+1)):
    printshops.append(pshop_id)
    pshop_id += 1
    if pshop_id == N_PRINTSHOPS:
        pshop_id=1
# Random user
consumer = "aaa"+str(random.randrange(1,MAX_CONSUMERS))

data={"files":{file_name:{"specs":[{"id":2,"name":"","paperSpecs":"","bindingSpecs":"","coverSpecs":"","deleted":False,"from":0,"to":0}],"pages":30,"name":file_name}},"printshops":printshops}

f = open(file_path,'rb')
data = {'files': open(file_path,'rb'), 'printRequest': json.dumps(data) }

r = requests.post(url, files=data, auth=(consumer,"1234"))
print r.text
f.close()

res = json.loads(r.text)
#print res['printRequestID']
r = requests.post(url, files=data, auth=(consumer,"1234"))

# Employees
emps = { 8:'mafalda', 6:'mariovdc' , 7:'mariovdm'}

# If some budget then choose that printshop
for pshop_id in res['budgets']:
    if len(res['budgets'][pshop_id]) < 6 and isinstance(float(res['budgets'][pshop_id]), numbers.Number):
        # consumer/printrequest/"+printRequestID+"/submit
        url = "http://localhost:8080/consumer/printrequest/"+str(res['printRequestID'])+"/submit"
        data = {"printRequestID": res['printRequestID'], "printshopID": int(pshop_id), "budget": float(res['budgets'][pshop_id]), "paymentMethod": "PROXYPRINT_PAYMENT"}
        r = requests.post(url, data=json.dumps(data), auth=(consumer,"1234"))
        print r.text
        # Change request status
        r = requests.post("http://localhost:8080/printshops/requests/"+str(res['printRequestID']), auth=((emps[int(pshop_id)]), "1234"))
        print r.text
        r = requests.post("http://localhost:8080/printshops/requests/"+str(res['printRequestID']), auth=((emps[int(pshop_id)]), "1234"))
        print r.text
        break;
