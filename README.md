# ensf400Project

## Instructions to Build and Run

1. **Build the Docker image:**
  ```sh
  docker-compose build
  ```

2. **Run the Docker container:**
  ```sh
  docker-compose up
  ```

3. **Access the application:**
  Open your web browser and go to `http://localhost:8080`

  ## Example Endpoints

  - **Get player information:**
    ```sh
    http://localhost:8080/api/player/8478402
    ```

  - **Compare two players:**
    ```sh
    http://localhost:8080/api/compare/8478402,8477492
    ```