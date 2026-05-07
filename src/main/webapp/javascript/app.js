const { useState, useEffect } = React;

// Handles search input and add film button
function TopBar({ searchTerm, setSearchTerm, onSearch, onAddFilm }) {
    return (
        <div className="w-full bg-white border-b-2 border-gray-300 flex justify-between items-center px-6 py-3">
            <div className="text-xl font-bold tracking-wide">
                <span className="text-red-500">Film</span>{" "}
                <span className="text-blue-500">Database</span>
            </div>

            <div className="flex items-center gap-2">
                <input
                    type="text"
                    placeholder="Search films"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)} // search by enter or clicking search
                    onKeyDown={(e) => {
                        if (e.key === "Enter") {
                            onSearch();
                        }
                    }}
                    className="border rounded px-3 py-1 w-64 focus:outline-none focus:ring-2 focus:ring-blue-400"
                />
                <button
                    onClick={onSearch}
                    className="px-3 py-1 bg-blue-500 text-white rounded hover:bg-blue-600"
                >
                    Search
                </button>
                <button
                    onClick={onAddFilm}
                    className="px-3 py-1 bg-green-500 text-white rounded hover:bg-green-600"
                >
                    Add Film
                </button>
            </div>
        </div>
    );
}

// Handles insert and update in a single form, controlled using film prop
function FilmForm({ film, onClose, onSave }) {
	// initialise form fields
    const [title, setTitle] = useState(film ? film.title : "");
    const [year, setYear] = useState(film ? film.year : "");
    const [director, setDirector] = useState(film ? film.director : "");
    const [stars, setStars] = useState(film ? film.stars : "");
    const [review, setReview] = useState(film ? film.review : "");
    const [error, setError] = useState("");

	// client side validation
    const handleSubmit = () => {
        if (!title.trim() || !year || !director.trim() || !stars.trim() || !review.trim()) {
            setError("All fields are required");
            return;
        }

        if (year <= 1900) {
            setError("Year must be a valid number");
            return;
        }

        const filmData = {
            id: film ? film.id : 0, // 0 as a placeholder for auto-incrementing IDs
            title: title.trim(),
            year: parseInt(year), // parseInt ensures its sent as a number not string
            director: director.trim(),
            stars: stars.trim(),
            review: review.trim()
        };

        onSave(filmData);
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded shadow-lg p-6 w-full max-w-md">
                <h2 className="text-xl font-bold mb-4">
                    {film ? "Edit Film" : "Add Film"}
                </h2>

                {error && <p className="text-red-500 text-sm mb-3">{error}</p>}

                <div className="flex flex-col gap-3">
                    <input
                        type="text"
                        placeholder="Title"
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                        className="border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400"
                    />
                    <input
                        type="number"
                        placeholder="Year"
                        value={year}
                        onChange={(e) => setYear(e.target.value)}
                        className="border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400"
                    />
                    <input
                        type="text"
                        placeholder="Director"
                        value={director}
                        onChange={(e) => setDirector(e.target.value)}
                        className="border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400"
                    />
                    <input
                        type="text"
                        placeholder="Stars"
                        value={stars}
                        onChange={(e) => setStars(e.target.value)}
                        className="border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400"
                    />
                    <textarea
                        placeholder="Review"
                        value={review}
                        onChange={(e) => setReview(e.target.value)}
                        className="border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400"
                        rows="3"
                    />
                </div>

                <div className="flex justify-end gap-2 mt-4">
                    <button
                        onClick={onClose}
                        className="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400"
                    >
                        Cancel
                    </button>
                    <button
                        onClick={handleSubmit}
                        className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
                    >
                        {film ? "Save Changes" : "Add Film"}
                    </button>
                </div>
            </div>
        </div>
    );
}

// Manages state and CRUD functionality
function App() {
    const [films, setFilms] = useState([]);
    const [format, setFormat] = useState("json");
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState("");
    const [searchTerm, setSearchTerm] = useState("");
    const [showForm, setShowForm] = useState(false);
    const [editingFilm, setEditingFilm] = useState(null);

	// reloads films whenever the format changes
    useEffect(() => {
        loadFilms();
    }, [format]);

	// fetches all films from the REST controller
    const loadFilms = () => {
        setLoading(true);

        fetch(`Filmsapi?format=${format}`)
            .then(response => response.text())
            .then(data => {
                console.log("Raw response:", data);

                if (format === "json") { // JSON parsed using JSON.parse
                    const parsed = JSON.parse(data);
                    setFilms(parsed);
                    setMessage("");
				} else if (format === "xml") { // XML parsed using DOMParser
					    const parser = new DOMParser();
					    const xml = parser.parseFromString(data, "text/xml");
					    const filmNodes = xml.querySelectorAll("film");
					    const parsed = Array.from(filmNodes).map(f => ({
					        id: f.querySelector("id").textContent,
					        title: f.querySelector("title").textContent,
					        year: f.querySelector("year").textContent,
					        director: f.querySelector("director").textContent,
					        stars: f.querySelector("stars").textContent,
					        review: f.querySelector("review").textContent
					    }));
					    setFilms(parsed);
					    setMessage("");
					} else { // text format, using new lines and seperated by pipe delimiters
						const lines = data.trim().split("\n");
						const parsed = lines.map(line => {
							const parts = line.split("|");
							return {
								id: parts[0],
								title: parts[1],
								year: parts[2],
								director: parts[3],
								stars: parts[4],
								review: parts[5],
							};
						});
						setFilms(parsed);
						setMessage("");
				}

                setLoading(false);
            })
            .catch(error => {
                console.error("Error:", error);
                setLoading(false);
                setMessage("Error loading films.");
            });
    };

	// searches films by title - falls back to all films if empty
	const searchFilms = () => {
	    setLoading(true);

		// encodeURIComponent ensures special characters in the search are properly encoded to avoid errors
	    const url = searchTerm.trim()
	        ? `Filmsapi?title=${encodeURIComponent(searchTerm.trim())}&format=${format}`
	        : `Filmsapi?format=${format}`;

	    fetch(url)
	        .then(response => response.text())
	        .then(data => {
	            if (format === "json") {
	                setFilms(JSON.parse(data));
	                setMessage("");
	            } else if (format === "xml") {
	                const parser = new DOMParser();
	                const xml = parser.parseFromString(data, "text/xml");
	                const filmNodes = xml.querySelectorAll("film");
	                const parsed = Array.from(filmNodes).map(f => ({
	                    id: f.querySelector("id").textContent,
	                    title: f.querySelector("title").textContent,
	                    year: f.querySelector("year").textContent,
	                    director: f.querySelector("director").textContent,
	                    stars: f.querySelector("stars").textContent,
	                    review: f.querySelector("review").textContent
	                }));
	                setFilms(parsed);
	                setMessage("");
	            } else {
	                const lines = data.trim().split("\n");
	                const parsed = lines.map(line => {
	                    const parts = line.split("|");
	                    return {
	                        id: parts[0],
	                        title: parts[1],
	                        year: parts[2],
	                        director: parts[3],
	                        stars: parts[4],
	                        review: parts[5]
	                    };
	                });
	                setFilms(parsed);
	                setMessage("");
	            }
	            setLoading(false);
	        })
	        .catch(error => {
	            console.error("Error:", error);
	            setLoading(false);
	            setMessage("Error searching films.");
	        });
	};

    const deleteFilm = (id) => {
        fetch(`Filmsapi?id=${id}`, { method: "DELETE" })
            .then(response => response.text())
            .then(data => {
                loadFilms();
            })
            .catch(error => {
                console.error("Error:", error);
            });
    };

    const saveFilm = (filmData) => {
        fetch("Filmsapi", {
            method: editingFilm ? "PUT" : "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(filmData)
        })
            .then(response => response.text())
            .then(data => {
                setShowForm(false);
                setEditingFilm(null);
                loadFilms();
            })
            .catch(error => {
                console.error("Error:", error);
            });
    };

    const openAddForm = () => {
        setEditingFilm(null);
        setShowForm(true);
    };

    const openEditForm = (film) => {
        setEditingFilm(film);
        setShowForm(true);
    };

    const closeForm = () => {
        setShowForm(false);
        setEditingFilm(null);
    };

    return (
        <div className="min-h-screen bg-gray-100">
            <TopBar
                searchTerm={searchTerm}
                setSearchTerm={setSearchTerm}
                onSearch={searchFilms}
                onAddFilm={openAddForm}
            />

            {showForm && (
                <FilmForm
                    film={editingFilm}
                    onClose={closeForm}
                    onSave={saveFilm}
                />
            )}

            <div className="w-full px-6 py-6">
                <div className="mb-4">
                    <label className="mr-2 font-semibold">Format:</label>
                    <select
                        className="p-2 border rounded bg-white"
                        value={format}
                        onChange={(e) => setFormat(e.target.value)}
                    >
                        <option value="json">JSON</option>
                        <option value="xml">XML</option>
                        <option value="text">Text</option>
                    </select>
                </div>

                {loading && <p className="text-gray-500 mb-4">Loading films...</p>}
                {message && <p className="text-sm text-blue-600 mb-4">{message}</p>}

                <div className="w-full bg-white shadow rounded p-4">
                    {films.length === 0 && !loading && !message && (
                        <p className="text-gray-500">No films to display</p>
                    )}

                    {films.map(film => (
                        <div key={film.id} className="border-b py-3 last:border-b-0">
                            <h2 className="font-bold text-lg">
                                {film.title} ({film.year})
                            </h2>
                            <p className="text-sm text-gray-600">
                                <span className="font-bold">Director:</span> {film.director}
                            </p>
                            <p className="text-sm text-gray-600">
                                <span className="font-bold">Stars:</span> {film.stars}
                            </p>
                            <p className="text-sm text-gray-600">
                                {film.review}
                            </p>
                            <div className="flex gap-2 mt-2">
                                <button
                                    onClick={() => openEditForm(film)}
                                    className="text-sm px-2 py-1 bg-gray-600 text-white rounded hover:bg-green-600"
                                >
                                    Edit
                                </button>
                                <button
                                    onClick={() => deleteFilm(film.id)}
                                    className="text-sm px-2 py-1 bg-gray-600 text-white rounded hover:bg-red-600"
                                >
                                    Delete
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}

ReactDOM.createRoot(document.getElementById("root")).render(<App />);