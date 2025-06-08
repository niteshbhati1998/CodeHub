# Removes lines with missing data and also lines with duplicate key values
import pandas as pd
import subprocess

filename = input("What is the filename? ")

df = pd.read_csv(filename, low_memory=False, verbose=True, warn_bad_lines=True, error_bad_lines=False)

null_counts = df.isnull().sum()
print(null_counts)
null_columns = df.columns[df.isnull().any()]
print(df[df.isnull().any(axis=1)][null_columns])

answer = input("Do you want to drop rows with NULL values (Y/N)? ")
if answer == "Y" or answer == "y":
    df = df.dropna(how='any',axis=0)
    df = df.drop_duplicates(subset=['timestring','unixtime'], keep='first')
    df["unixtime"] = df["unixtime"].astype(int)
    df["inRH"] = df["inRH"].astype(int)
    df.to_csv("data-clean.csv", index=False, header=0)
    subprocess.call([r"sed -i -e 's/^\([0-9]\{4\}-[0-9][0-9]-[0-9][0-9]\) \([0-9][0-9]\:[0-9][0-9]\:[0-9][0-9]\)/\1,\2/g' data-clean.csv"], shell=True)
    subprocess.call([r"sed -i -e '1s;^;yyyy-mm-dd,time,unixtime,inTempC,inTempF,outTempC,outTempF,pressure,inRH,outDewptF,avgWind,gustWind\n;' data-clean.csv"], shell=True)
else:
    exit()