#!/bin/bash
# k8s-health-check.sh
# k8sgpt로 스캔 → 이상 있으면 holmes에 분석 맡기기

set -euo pipefail

source ~/.key
source ~/.path

NAMESPACE="${1:-}"
FILTER_ARG=""
if [ -n "$NAMESPACE" ]; then
    FILTER_ARG="-n $NAMESPACE"
fi

echo "🔍 k8sgpt scanning..."
ANALYSIS=$(k8sgpt analyze --explain $FILTER_ARG -o json 2>/dev/null)

STATUS=$(echo "$ANALYSIS" | jq -r '.status')
PROBLEMS=$(echo "$ANALYSIS" | jq -r '.problems // 0')

if [ "$STATUS" = "ProblemDetected" ] && [ "$PROBLEMS" -gt 0 ]; then
    echo "⚠️  $PROBLEMS issue(s) detected. Sending to Holmes..."

    # k8sgpt 결과를 요약해서 holmes에 넘김
    SUMMARY=$(echo "$ANALYSIS" | jq -r '
        .results[]
        | select(.error != null)
        | "\(.kind)/\(.name): \(.error[0].Text)\n  Details: \(.details)"
    ' | grep -v "kube-root-ca.crt" || true)

    if [ -z "$SUMMARY" ]; then
        echo "✅ No actionable issues (only benign kube-root-ca.crt warnings)."
        exit 0
    fi

    echo "$SUMMARY"
    echo ""
    echo "---"
    echo "🤖 Holmes analyzing..."

    holmes ask "Analyze these Kubernetes issues and provide root cause + fix recommendations for each:

$SUMMARY" --no-interactive
else
    echo "✅ No issues detected."
fi
