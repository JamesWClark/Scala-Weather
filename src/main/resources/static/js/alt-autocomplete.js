document.addEventListener('DOMContentLoaded', function () {
    var input = document.getElementById('city');
    var suggestionsContainer = document.createElement('div');
    suggestionsContainer.className = 'autocomplete-suggestions';
    input.parentNode.appendChild(suggestionsContainer);

    let debounceTimeout;

    input.addEventListener('input', function () {
        clearTimeout(debounceTimeout);
        debounceTimeout = setTimeout(() => {
            var query = input.value;
            if (query.length < 3) {
                suggestionsContainer.innerHTML = '';
                return; // Wait until the user has typed at least 3 characters
            }

            fetch(`/autocomplete?query=${encodeURIComponent(query)}`)
                .then(response => response.json())
                .then(data => {
                    var results = data.items; // Adjusted to match the expected response structure
                    suggestionsContainer.innerHTML = '';
                    results.forEach(result => {
                        var suggestion = document.createElement('div');
                        suggestion.className = 'autocomplete-suggestion';
                        suggestion.textContent = result.address.label; // Adjusted to match the expected response structure
                        suggestion.addEventListener('click', function () {
                            input.value = result.address.label; // Adjusted to match the expected response structure
                            suggestionsContainer.innerHTML = '';
                        });
                        suggestionsContainer.appendChild(suggestion);
                    });
                })
                .catch(error => console.error('Error fetching autocomplete results:', error));
        }, 300); // Debounce delay of 300ms
    });
});