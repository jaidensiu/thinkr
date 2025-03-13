import axios from 'axios';
import dotenv from 'dotenv';

dotenv.config();

const API_URL = process.env.PRODUCTION_URL;
const MAX_ALLOWED_TIME = 11.3;

/**
 * @group nfr
 */
describe('NFR Suggested Materials Performance Test', () => {
    jest.setTimeout(15000);

    it(`should fetch suggested materials in less than ${MAX_ALLOWED_TIME} seconds`, async () => {
        const userId = 'test-user-123';
        const startTime = Date.now();

        const response = await axios.get(
            `${API_URL}/study/suggestedMaterials`,
            {
                params: {
                    userId: userId,
                    limit: 5,
                },
            }
        );

        const endTime = Date.now();
        const duration = (endTime - startTime) / 1000;

        console.log(
            `\nPERFORMANCE SUMMARY: Suggested Materials Request
       -----------------------------------------------
       ✧ Response Time:      ${duration.toFixed(2)} seconds
       ✧ Threshold:          ${MAX_ALLOWED_TIME} seconds
       ✧ Performance Margin: ${(MAX_ALLOWED_TIME - duration).toFixed(2)} seconds
       ✧ Status:             ${duration <= MAX_ALLOWED_TIME ? 'PASSED ✅' : 'FAILED ❌'}
       -----------------------------------------------`
        );

        expect(response.status).toBe(200);
        expect(duration).toBeLessThanOrEqual(MAX_ALLOWED_TIME);
    });
});
