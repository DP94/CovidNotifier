import time
import urllib.parse
import requests
import schedule
import smtplib
from datetime import datetime

nations: [] = ["England", "Scotland", "Wales", "Northern Ireland"]
host: str = "smtp.gmail.com"
port: int = 587
to: str = "to_email_goes_here"
from_address: str = "from_email_goes_here"
password: str = "password_goes_here"
date_format: str = "%a, %d %b %Y %H:%M:%S %Z"
short_date_format: str = "%Y %m %d"


def get_covid_job():
    response = {}
    try:
        print(f"Scheduled stats job running at {datetime.now()}")
        params = urllib.parse.urlencode(
            {'filters': 'areaType=nation',
             'structure': '{"date":"date","newCasesByPublishDate":"newCasesByPublishDate"}',
             'latestBy': 'newCasesByPublishDate'})
        response = requests.get("https://api.coronavirus.data.gov.uk/v1/data?" + params)
        last_modified = response.headers.get("Last-Modified")
        last_updated_date = datetime.strptime(last_modified, date_format).strftime(short_date_format)
        now = datetime.now().strftime(short_date_format)
        if last_updated_date < now:
            print(f'Data out of date, but latest is not available at the usual time. Current check time is {datetime.now}; recursively checking until published')
            time.sleep(1800)
            get_covid_job()
        else:
            process_stats(response)
    except Exception as exception:
        print(f"Error occurred whilst trying to fetch latest data: {exception}")
    finally:
        if response is not None:
            response.close()


def process_stats(response):
    json_string = response.json()
    data = 0
    for i in range(len(nations)):
        data += json_string["data"][i]["newCasesByPublishDate"]
    email_stats(data)


def email_stats(cases):
    print(cases)
    server = smtplib.SMTP(host, port)
    server.ehlo()
    server.starttls()
    server.login(from_address, password)
    server.sendmail(from_address, to, get_email_text(cases))


def get_email_text(cases):
    return '\r\n'.join(
        [f'To: {to}', f'From: {from_address}', f'Subject: Corona daily cases', f'The daily cases today are {cases}'])


schedule.every().day.at("16:30").do(get_covid_job)
while 1:
    schedule.run_pending()
    time.sleep(1)
