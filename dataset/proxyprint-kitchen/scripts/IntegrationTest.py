import requests
import json
import webbrowser

# Route Rest
url = "http://localhost:8080/printdocument"

# File location
file_path = "/Users/MGonc/Desktop/HTML.pdf"

data = {'files': open(file_path,'rb')}

r = requests.post(url, files=data)

id = r.json().get('printRequestID')

# Open page to finish request
url = "http://localhost:9000/#/printdocument/" + str(id)
webbrowser.open(url)