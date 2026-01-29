const app = require('./app');
const ngrok = require('@ngrok/ngrok');
const PORT = process.env.PORT || 3000;

app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);

    // use ngrok in package npm 
    ngrok.connect({
        addr: PORT,
        authtoken_from_env: true
    })
        .then(listener => console.log(`Ingress established at: ${listener.url()}`))
        .catch(err => console.error('Error establishing ngrok tunnel:', err));
});
