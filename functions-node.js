var http = require('https');


function getPromise(id) {
    console.log(id);
    let url = 'https://dadosabertos.camara.leg.br/api/v2/deputados/' + id;
	return new Promise((resolve, reject) => {
		http.get(url, (response) => {
			let chunks_of_data = [];

			response.on('data', (fragments) => {
				chunks_of_data.push(fragments);
			});

			response.on('end', () => {
				let response_body = Buffer.concat(chunks_of_data);
				resolve(response_body.toString());
			});

			response.on('error', (error) => {
				reject(error);
			});
		});
	});
}


exports.handler = async (event) => {
    return {
        statusCode: 200,
        body: await getPromise(event.body),
    };
};