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

Each registration attempt is stored into a database. The "success" status is by default false, until it's changed in case of successfully registration (if it's the case).
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

![image](https://github.com/BalteanuAndrei709/HospitalInternalApp/assets/79245195/5e133898-2659-4e4c-851c-d242483e8385)

</picture>

#### Social number matches given data

The romanian social number is composed from multiple parts.
<picture>

![image](https://github.com/BalteanuAndrei709/HospitalInternalApp/assets/79245195/da79c26f-07f2-4bd2-a6dd-dd026ebe804a)

</picture>
