from locust import HttpLocust, TaskSet, task
import requests
import random
import json
import numbers

# Notes: Comment out lines 195/6, 392/3 from PrintShopController.java to not send emails
# Global data configs
N_PRINTSHOPS=8
MAX_ALLOWED_BUDGETS=5
MAX_PRINTING_SCHEMAS=5
MAX_CONSUMERS=1000
MAX_LATITUDE=90
MAX_LONGITUDE=180
FILES_POOL = "/home/daniel/Desktop/files_pool/"

class UserBehavior(TaskSet):

    """
    Login
    """
    @task(2)
    def login(self):
        data = {"username": "joao", "password": "1234"}
        self.client.post("/login",data=data)

    """
    List all printshops
    """
    @task(1)
    def printshops(self):
        self.client.get("/printshops")

    """
    Print Request 1st step - Get nearest printshops
    """
    @task(4)
    def get_nearest_printshops(self):
        data = {"latitude": random.randrange(1,MAX_LATITUDE+1), "longitude": random.randrange(1,MAX_LONGITUDE+1)}
        self.client.get("/printshops/nearest",data=data)

    """
    Print Request - from 2nd step to employee change status to FINISHED
    """
    @task(3)
    def get_budgets(self):
        # Files location and file selection
        file_name = str(random.randrange(1,17))+".pdf"
        file_path = FILES_POOL+file_name
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
        # Random printing schema
        pschema_id = random.randrange(1,MAX_PRINTING_SCHEMAS+1)
        # Construct payload
        payload={"files":{file_name:{"specs":[{"id":pschema_id,"name":"","paperSpecs":"","bindingSpecs":"","coverSpecs":"","deleted":False,"from":0,"to":0}],"pages":30,"name":file_name}},"printshops":printshops}
        f = open(file_path,'rb')
        data = {'files': f, 'printRequest': json.dumps(payload) }
        # Get budgets
        r = self.client.post("/consumer/budget", files=data, auth=(consumer,"1234"), catch_response=True)
        self.client.post("/consumer/budget", files=data, auth=(consumer,"1234"))
        res = json.loads(r.text)
        f.close()
        # Choose some valid budget if existent
        if 'success' in res:
            for pshop_id in res['budgets']:
                if len(res['budgets'][pshop_id]) < 6 and isinstance(float(res['budgets'][pshop_id]), numbers.Number):
                    data = {"printRequestID": res['printRequestID'], "printshopID": int(pshop_id), "budget": float(res['budgets'][pshop_id]), "paymentMethod": "PROXYPRINT_PAYMENT"}
                    # Submit request
                    self.client.post("/consumer/printrequest/%s/submit" % str(res['printRequestID']), name="/consumer/printrequest/[prid]/submit", data=json.dumps(data), auth=(consumer,"1234"))
                    break;

class ProxyPrintUser(HttpLocust):
    task_set = UserBehavior
    min_wait=5000
    max_wait=9000
