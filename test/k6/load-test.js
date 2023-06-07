import http from 'k6/http';
import { sleep, check } from 'k6';

export let options = {
    vus: 10,  // number of virtual users
    duration: '30s',  // test duration
};

export default function () {
    // Your GET endpoint
    let getResponse = http.get('http://localhost:8080/api/patients/');

    check(getResponse, {
        'GET request status was 200': (r) => r.status === 200,
    });

    // Your POST endpoint
    let postPayload = JSON.stringify({
        // your POST request payload
        name: 'hello',
        gender: 'Male',
        date_of_birth: '2002-03-29',
        address: '222 Pine St',
        phone_number: '232 999 3333'
    });

    let postParams = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    let postResponse = http.post('http://localhost:8080/api/patients/', postPayload, postParams);
    //console.log(JSON.stringify(postResponse))

    check(postResponse, {
        'POST request status was 200': (r) => r.status === 200,
    });

    sleep(1); // sleep for 1 second between iterations
}
