# Preview
This is personal project created for learning Apache Kafka.

This application is intended to be used in a hospital. For now, it allows registering the patients and allocating the saloons bases on the patient needs.

## Apache Kafka

Because the application would be used in a hospital, "down-times" need to be as low as possible (if not actually none). For that, the Kafka Cluster contains 3 brokers
and each topic has an replication factor of 3 with 3 partitions. 
The communication through different services is done using Apache Kafka.

### Register Service

It receives the patient details on the following API:
```
http://localhost:8084/api/registration
```
The POST request could look like:
```
{
  "name": "Balteanu Andrei",
  "socialNumber": "5000113000000",
  "county": "Iasi",
  "series": "MZ",
  "dateOfBirth": "13/01/2000",
  "sex": "M",
  "department":"GASTROENTEROLOGY",
  "respiratoryProblems": false,
  "mobilityProblems": false,
  "visionProblems": true
}
```

The registration needs multiple validations in order to be completed. Each validation is done by a different service:
* InsuranceService: checks the insured status of the patient.
* IndetityCardService: checks if the personal details of the patient match (county with the series, socialnumber with gender,county, year of birth etc.)
* HospitalServiec: checks if there are any available saloons that have the requirments for the specific patient (facilities for special respiratori, vision or mobility problems)

The flow for registering tha patient looks like:
<picture>

![image](https://github.com/BalteanuAndrei709/HospitalInternalApp/assets/79245195/31462ae2-c4be-493c-a50f-7a0cf2f2e353)

</picture>

Each registration attempt is stored into a database. The "success" status is by default false, until it's changed in case of successfully registration (if it's the case). If all checks are passed, the request returns the saloon number in which the patient will be treated.
<picture>

![image](https://github.com/BalteanuAndrei709/HospitalInternalApp/assets/79245195/d9cbd3d2-69b6-4f94-954b-1767e8323c53)

</picture>

### Insurance Service

The kafka listener receives an unique indetifier and the social number of the patient that needs to be checked. Using the example request from above, the message would
look like:
```
InsuranceDetailsDTO(identifier=f90667cc-7508-4650-8639-310ed91500fc, socialNumber=5000113000000)
```

This service communicates with a databases that store the insured status of each citisen. The table has the following structure:

<picture>

![image](https://github.com/BalteanuAndrei709/HospitalInternalApp/assets/79245195/306f4293-f42c-4743-a296-0440ded0a2a3)

</picture>

The service return the insured status into a different topic, from which the Register Service reads.

### Identity Card Service

The kafka listener receives all the personal details of the patient (excluding the special problems). In this service are 2 checks done:
1. If the county matche the series (judetul face match cu seria buletinului)
2. If the social number matches the given data (date of birth, county, gender)

#### County matcher series

Each county has a list of series possible for the identity card. As for example, for "IASI" county there are 3 possible series: MX, MZ, IZ (each series for each county
can be found here : https://ro.wikipedia.org/wiki/Carte_de_identitate_rom%C3%A2neasc%C4%83). 

For this, this service communicates with a service that stores each county, allong with the possible series for it:

<picture>

![image](https://github.com/BalteanuAndrei709/HospitalInternalApp/assets/79245195/a06967e8-f4e5-4fa4-a1a3-a7027ca9cfb4)

</picture>

#### Social number matches given data

The romanian social number is composed from multiple parts.
<picture>

![image](https://github.com/BalteanuAndrei709/HospitalInternalApp/assets/79245195/da79c26f-07f2-4bd2-a6dd-dd026ebe804a)

</picture>

1. S component: this component is calculated based on the patient gender and year of birth. It has 8 possible values:
  - *1*: male gender and year birth between 1900 - 1999
  - *2*: female gender and year birth between 1900 - 1999
  - *3*: male gender and year birth between 1800 - 1899
  - *4*: female gender and year birth between 1800 - 1899
  - *5*: male gender and year birth between  2000 - 2099
  - *6*: female gender and year birth between 2000 - 2099
  - *7*: male gender for resident persons (persoane residente)
  - *8*: female gender for resident persons (persoane residente)

2. AA component: last 2 digits of the year of birth.
3. LL component: the month when the citisen was born, with values between 01 and 12.
4. ZZ component: day of birth, with values between 01 and 31.
5. JJ component: it represent the county code where the citisen was porn. A complete list:
6. NNN component: a random number between 001 and 999.
7. C component: a random digit.

More information can be found on : https://ro.wikipedia.org/wiki/Cod_numeric_personal_(Rom%C3%A2nia)

Based on the given data at registration (the date of birth, the county and series in which the citisen was born, the gender), we can check if the social number is valid. 

The response of both checks is send back to the Register Service.

#### Hospital Service

This service tries to allocate a saloon to the patient. The allocated saloon will need to have as less facilities possible, but metting the patient special needs (respiratori, mobility or vision problems). If there is any available saloon, the number of it will be send back to the Register Service.
  
<picture>

![image](https://github.com/BalteanuAndrei709/HospitalInternalApp/assets/79245195/3794031f-2033-4286-b7a9-fa9729ea3698)

</picture>


