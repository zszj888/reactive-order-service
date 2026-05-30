import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '10s', target: 50 },
        { duration: '20s', target: 200 },
        { duration: '10s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<100', 'p(99)<200'],
        http_req_failed: ['rate<0.01'],
    },
};

export default function () {
    const payload = JSON.stringify({
        userId: Math.floor(Math.random() * 10000) + 1,
        productId: 1,
        quantity: 1,
    });

    const res = http.post(
        'http://localhost:8080/orders',
        payload,
        {
            headers: {
                'Content-Type': 'application/json',
            },
        }
    );

    check(res, {
        'status is 200': (r) => r.status === 200,
        'status is 409 (sold out)': (r) => r.status === 409,
    });

    sleep(0.1);
}
