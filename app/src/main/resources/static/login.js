function authenticate() {
    var email = document.getElementById("email").value;
    var password = document.getElementById("password").value;

    var formData = {
        email: email,
        password: password
    };

    fetch("http://localhost:8080/api/auth/authenticate", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(formData)
    })
        .then(function (response) {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error("Authentication failed");
            }
        })
        .then(function (data) {
            console.log("Authentication response:", data);
            // Save the token to browser memory (e.g., local storage)
            localStorage.setItem("token", data.token);
            // Redirect to the desired page or perform any other action
            window.location.href = "/home"; // Example redirect to dashboard page
        })
        .catch(function (error) {
            console.error("Authentication error:", error);
        });
}
