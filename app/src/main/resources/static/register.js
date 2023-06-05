function register() {
    var email = document.getElementById("email").value;
    var password = document.getElementById("password").value;
    var confirmPassword = document.getElementById("confirm-password").value;

    if (password !== confirmPassword) {
        console.error("Passwords do not match");
        return;
    }

    var formData = {
        email: email,
        password: password,
        confirmPassword: confirmPassword
    };

    fetch("http://localhost:8080/api/auth/register", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(formData)
    })
        .then(function (response) {
            if (response.ok) {
                console.log("Registration successful");
                // Redirect to the desired page or perform any other action
            } else {
                throw new Error("Registration failed");
            }
        })
        .catch(function (error) {
            console.error("Registration error:", error);
        });
}