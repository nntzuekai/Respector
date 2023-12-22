import json

f=open('swagger.json')

data=json.load(f)

paths=data['paths']


for p in paths:
    print(p)
    for m in paths[p]:
        if 'parameters' in paths[p][m]:
            print('\t%s:%d'%(m,len(paths[p][m]['parameters'])))
        else:
            print('\t%s:%d'%(m,0))
            
