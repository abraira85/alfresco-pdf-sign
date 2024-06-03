#!/bin/bash

if [ -z "${M2_HOME}" ]; then
  export MVN_EXEC="mvn"
else
  export MVN_EXEC="${M2_HOME}/bin/mvn"
fi

# Function to colorize text
colorize() {
    local color="$1"
    local text="$2"
    case "$color" in
        green)
            echo -e "\033[0;32m${text}\033[0m"
            ;;
        red)
            echo -e "\033[0;31m${text}\033[0m"
            ;;
        yellow)
            echo -e "\033[0;33m${text}\033[0m"
            ;;
        *)
            echo "$text"
            ;;
    esac
}

status() {
    # Function to calculate the maximum length of each column
    max_length() {
        awk -v column="$1" '{if (length($column) > max) max = length($column)} END {print max}'
    }

    # Get the maximum lengths of each column
    max_id=$(docker compose ps -a --format "{{.ID}}" | max_length 1)
    max_name=$(docker compose ps -a --format "{{.Names}}" | max_length 1)

    # Calculate the maximum length of the status column
    statuses=$(docker compose ps -a --format "{{.Status}}")
    max_status=$(echo "$statuses" | grep -oE "(running|paused|exited)" | max_length 1)

    # Check if there are no containers
    if [ -z "$statuses" ]; then
        echo " "
        echo "No containers are running or stopped. Please run 'start' command."
        echo " "
        return
    fi

    # Calculate the total width of the table with some additional padding
    total_width=$((max_id + max_name + max_status + 21))

    # Print the table header
    printf "%s\n" "┌$(printf '─%.0s' $(seq 1 $total_width))┐"
    printf "│ %-*s │ %-*s │ %-*s │ %-*s   │\n" "3" "NUM" "$max_id" "ID" "$max_name" "SERVICE" "$max_status" "STATE"
    printf "%s\n" "├$(printf '─%.0s' $(seq 1 $total_width))┤"

    # Counter for service number
    service_num=1

    # Print the table body
    docker compose ps -a --format "{{.ID}} {{.Names}} {{.Status}}" | while read -r line; do
        container_id=$(echo "$line" | awk '{print $1}')
        container_name=$(echo "$line" | awk '{print $2}')
        container_status=$(echo "$line" | awk '{$1=$2=""; print $0}')

        state="running"

        # Check if the container is paused
        if echo "$container_status" | grep -q "Paused"; then
            state="paused"
        fi

        # Check if the container is stopped
        if echo "$container_status" | grep -q "Exited"; then
            state="stopped"
        fi

        # Check if the container is in the services array
        if [ "$state" = "running" ]; then
            state=$(colorize green "$state")
        elif [ "$state" = "paused" ]; then
            state=$(colorize yellow "$state ")
        else
            state=$(colorize red "$state")
        fi

        printf "│ %-*s │ %-*s │ %-*s │ %-*s │\n" "3" "$service_num" "$max_id" "$container_id" "$max_name" "$container_name" "$max_status" "$state"

        # Increment service number
        ((service_num++))
    done
    printf "%s\n" "└$(printf '─%.0s' $(seq 1 $total_width))┘"
}

start() {
    local build=0
    local verbose=0
    local services_to_start=()

    # Parse command-line options
    while [ "$1" != "" ]; do
        case $1 in
            -b | --build )
                build=1
                ;;
            -v | --verbose )
                verbose=1
                ;;
            [0-9]*)
                services_to_start+=("$1")
                ;;
            *)
                echo "Invalid option: $1"
                return 1
                ;;
        esac
        shift
    done

    # Get the container IDs without printing them
    container_info=$(docker compose ps -a --format "{{.ID}} {{.Names}}")

    service_num=$(echo "$container_info" | wc -l)

    # Build if necessary
    if [ $build -eq 1 ]; then
        # build
        docker compose up --build -d
    fi

    if [ ${#services_to_start[@]} -gt 0 ]; then
        for index in "${services_to_start[@]}"; do
            # Ensure index is within valid range
            if [ "$index" -ge 1 ] && [ "$index" -le "$service_num" ]; then
                # Get the container ID corresponding to the index
                container_id=$(echo "$container_info" | sed -n "${index}p" | awk '{print $2}')
                docker compose up -d "$container_id"
            else
                echo "Invalid index: $index"
            fi
        done
    else
        docker compose up -d
    fi

    # Show logs if requested
    if [ $verbose -eq 1 ]; then
        tail
    fi
}

stop() {
    local purge=0
    local services_to_stop=()

    # Get the container IDs without printing them
    container_info=$(docker compose ps -a --format "{{.Names}}")

    while [ "$1" != "" ]; do
        case $1 in
            -p | --purge )
                purge=1
                ;;
            [0-9]*)
                services_to_stop+=("$1")
                ;;
            *)
                echo "Invalid option: $1"
                return 1
                ;;
        esac
        shift
    done

    service_num=$(echo "$container_info" | wc -l)

    if [ $purge -eq 1 ]; then
        docker compose down -v
    elif [ ${#services_to_stop[@]} -gt 0 ]; then
        for index in "${services_to_stop[@]}"; do
            # Ensure index is within valid range
            if [ "$index" -ge 1 ] && [ "$index" -le "$service_num" ]; then
                # Get the container ID corresponding to the index
                container_id=$(echo "$container_info" | sed -n "${index}p" | awk '{print $1}')
                echo ${container_id}
                docker compose stop $container_id
            else
                echo "Invalid index: $index"
            fi
        done
    else
        docker compose stop
    fi
}

restart() {
    local services_to_restart=()

    # Parse command-line options
    while [ "$1" != "" ]; do
        case $1 in
            [0-9]*)
                services_to_restart+=("$1")
                ;;
            *)
                echo "Invalid option: $1"
                return 1
                ;;
        esac
        shift
    done

    # Get the container IDs without printing them
    container_info=$(docker compose ps -a --format "{{.ID}} {{.Names}}")

    service_num=$(echo "$container_info" | wc -l)

    if [ ${#services_to_restart[@]} -gt 0 ]; then
        for index in "${services_to_restart[@]}"; do
            # Ensure index is within valid range
            if [ "$index" -ge 1 ] && [ "$index" -le "$service_num" ]; then
                # Get the container ID corresponding to the index
                container_id=$(echo "$container_info" | sed -n "${index}p" | awk '{print $2}')
                docker compose restart "$container_id"
            else
                echo "Invalid index: $index"
            fi
        done
    else
        docker compose restart
    fi
}

build() {
    $MVN_EXEC clean install -DskipTests=true
}

tail() {
    local services_to_tail=()

    # Parse command-line options
    while [ "$1" != "" ]; do
        case $1 in
            [0-9]*)
                services_to_tail+=("$1")
                ;;
            *)
                echo "Invalid option: $1"
                return 1
                ;;
        esac
        shift
    done

    # Get the container IDs without printing them
    container_info=$(docker compose ps -a --format "{{.ID}} {{.Names}}")

    service_num=$(echo "$container_info" | wc -l)

    if [ ${#services_to_tail[@]} -gt 0 ]; then
        for index in "${services_to_tail[@]}"; do
            # Ensure index is within valid range
            if [ "$index" -ge 1 ] && [ "$index" -le "$service_num" ]; then
                # Get the container name corresponding to the index
                container_name=$(echo "$container_info" | sed -n "${index}p" | awk '{print $2}')
                docker compose logs -f "$container_name"
            else
                echo "Invalid index: $index"
            fi
        done
    else
        docker compose logs -f
    fi
}

test() {
    local build=0
    while [ "$1" != "" ]; do
        case $1 in
            -b | --build )
                build=1
                ;;
        esac
        shift
    done

    if [ $build -eq 1 ]; then
        build
    fi
    $MVN_EXEC verify
}

help() {
    echo "Usage: $0 {command} [options] [service_numbers]"
    echo
    echo "Commands:"
    echo "  start        Start the containers and optionally tail the logs."
    echo " "
    echo "               Options:"
    echo "                 -b, --build    Build the project before starting."
    echo "                 -v, --verbose  Show logs after starting."
    echo " "
    echo "               service_numbers: Specify container numbers to start."
    echo " "
    echo "  stop         Stop the containers."
    echo " "
    echo "               Options:"
    echo "                 -p, --purge    Remove volumes when stopping."
    echo " "
    echo "               service_numbers: Specify container numbers to stop."
    echo " "
    echo "  restart      Restart all or specified containers."
    echo " "
    echo "               service_numbers: Specify container numbers to restart."
    echo " "
    echo "  tail         Tail the logs of running containers."
    echo " "
    echo "               service_numbers: Specify container numbers to tail logs."
    echo " "
    echo "  test         Run tests."
    echo " "
    echo "               Options:"
    echo "                 -b, --build    Build the project before testing."
    echo " "
    echo "  status       Show the names and statuses of running services."
    echo " "
    echo "  help         Show this help message."
    echo " "
}

case "$1" in
  start)
    shift
    start "$@"
    ;;
  stop)
    shift
    stop "$@"
    ;;
  restart)
    shift
    restart "$@"
    ;;
  tail)
    shift
    tail "$@"
    ;;
  test)
    shift
    test "$@"
    ;;
  status)
    status
    ;;
  help)
    help
    ;;
  *)
    help
esac
